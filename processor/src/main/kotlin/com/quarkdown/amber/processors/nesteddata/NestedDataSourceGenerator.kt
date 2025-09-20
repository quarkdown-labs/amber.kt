package com.quarkdown.amber.processors.nesteddata

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.quarkdown.amber.processor.GenerationConstants.INDENT
import com.quarkdown.amber.processor.dataclass.DataClassPropertyNode
import com.quarkdown.amber.processor.dataclass.PropertyNode
import com.quarkdown.amber.processor.dataclass.buildDataClassPropertiesTree
import com.quarkdown.amber.processor.generator.ClassSourceGenerator
import com.quarkdown.amber.processor.utils.KDocUtils

/** The name of the generated deep copy extension function. */
private const val FUNCTION_NAME = "deepCopy"

private const val FUNCTION_KDOC = """
Creates a deep copy of this data class, with optional modification of nested properties.

This generated function exposes parameters for every property in the data class,
including nested properties of other data class types.

Example usage:
```
val original = OuterData(inner = InnerData(value = 42))
val modified = original.deepCopy(innerValue = 100)
// Result: OuterData(inner = InnerData(value = 100))
```

Note: This function deep-copies only data classes and their properties.
It does not handle collections or other complex types on its own.

Note: Consider the following:
```
data class First(val second: Second)
data class Second(val third: Third)
data class Third(val value: Int)
```

Nested parameters will take precedence over parent parameters:
```
val first = First(Second(Third(42)))
val modified = first.deepCopy(second = Second(Third(100)), secondThirdValue = 200)
// Result: First(Second(Third(200)))
```

Note: A nested property cannot be set if any of its parent properties are `null`:
```
data class First(val second: Second?)
data class Second(val value: Int)

val first = First(null)
first.deepCopy(secondValue = 100) // First(null)
first.deepCopy(second = Second(100)) // First(Second(100))
first.deepCopy(second = Second(100), secondValue = 200) // First(Second(200))
```

@return A new instance of the data class with updated properties as specified.
"""

/**
 * Generates an extension function that performs a deep copy of a data class by
 * exposing parameters for nested properties.
 *
 * The generator walks nested data-class properties and emits parameters for every
 * leaf and intermediate node. It then emits code that rebuilds the nested data
 * objects using `copy(...)` calls, applying null-safety where needed.
 *
 * @param environment The KSP processing environment
 * @param annotated The data class declaration to process
 */
class NestedDataSourceGenerator(
    environment: SymbolProcessorEnvironment,
    annotated: KSClassDeclaration,
) : ClassSourceGenerator(environment, annotated) {
    override val fileNameSuffix: String
        get() = "NestedData"

    override fun generateSourceBody(annotated: KSClassDeclaration): String =
        buildString {
            val propertiesTree = annotated.buildDataClassPropertiesTree()
            val params = buildParameterList(propertiesTree)
            appendLine(KDocUtils.generate(FUNCTION_KDOC))
            append(signatureLine(propertiesTree, params))
            appendLine(" {")
            appendLine(methodBody(propertiesTree, params))
            appendLine("}")
        }

    /**
     * A parameter in the generated function signature.
     *
     * @property name The flattened parameter name, where each parent parameter is joined in camelCase
     *                (e.g., `firstSecondThird` for `first.second.third`)
     * @property node The property node this parameter represents
     * @property parents The chain of parent nodes leading to this property
     */
    private data class Parameter(
        val name: String,
        val node: PropertyNode,
        val parents: List<PropertyNode>,
    ) {
        /** True if any parent in the property chain is nullable. */
        val hasNullableParents: Boolean
            get() = parents.any { it.nullable }
    }

    /**
     * Given a node and its parent chain of data class properties, produces a flattened
     * property name suitable for use as a parameter name, camelCase.
     * @param parents The chain of parent property nodes
     * @param node The current property node
     * @return The flattened property name (e.g., `firstSecondThird` for `first.second.third`)
     */
    private fun flattenPropertyName(
        parents: List<PropertyNode>,
        node: PropertyNode,
    ): String =
        (parents.asSequence().map { it.name } + node.name)
            .mapIndexed { index, part ->
                if (index == 0) part else part.replaceFirstChar { it.uppercaseChar() }
            }.joinToString(separator = "")

    /**
     * Builds the complete list of parameters for the generated function.
     *
     * @param propertiesTreeRoot The root node of the property tree
     * @return A list of flattened parameters representing every property in the tree
     */
    private fun buildParameterList(propertiesTreeRoot: DataClassPropertyNode): List<Parameter> {
        val params = mutableListOf<Parameter>()

        fun traverseProperties(
            node: PropertyNode,
            parents: List<PropertyNode>,
        ) {
            params +=
                Parameter(
                    name = flattenPropertyName(parents, node),
                    node = node,
                    parents = parents,
                )

            if (node is DataClassPropertyNode) {
                for (child in node.children) {
                    traverseProperties(child, parents + node)
                }
            }
        }

        propertiesTreeRoot.children.forEach {
            traverseProperties(it, emptyList())
        }

        return params
    }

    /**
     * Generates the function signature line including parameter declarations.
     *
     * @param root The root property node representing the class
     * @param params List of all parameters for the function
     * @return The complete function signature line
     */
    private fun signatureLine(
        root: PropertyNode,
        params: List<Parameter>,
    ): String {
        val className = root.name

        val paramsSignature =
            buildString {
                params.forEach { param ->
                    // Output examples:
                    // first: First = this.first,,
                    // firstSecondThird: Third = firstSecond.third,
                    // firstSecondThird: Third? = firstSecond.third,
                    // firstSecondThird: Third? = firstSecond?.third,
                    append(INDENT)
                    append(param.name)
                    append(": ")
                    append(param.node.type)
                    if (param.node.nullable || param.hasNullableParents) append("?")
                    append(" = ")

                    // For example, the property chain `first.second.third` has the `firstSecondThird` parameter,
                    // where its parent parameter is `firstSecond` and grandparent is `first`.
                    val parentParam = params.find { it.node === param.parents.lastOrNull() }

                    append(parentParam?.name ?: "this")
                    if (param.hasNullableParents) append("?")
                    append(".")
                    append(param.node.name)
                    appendLine(",")
                }
            }

        return "fun $className.$FUNCTION_NAME(\n$paramsSignature): $className"
    }

    /**
     * Generates the `copy` call for reconstructing nested data structures.
     *
     * @param root The root data class node being copied
     * @param params All parameters available for the copy operation
     * @param parents Chain of parent parameters for null safety logic
     * @return The `copy(...)` call as a string
     */
    private fun copyData(
        root: DataClassPropertyNode,
        params: List<Parameter>,
        parents: List<Parameter> = emptyList(),
    ): String =
        buildString {
            append("copy(")

            root.children.forEachIndexed { index, node ->
                val param = params.first { it.node === node }

                append(node.name)
                append(" = ")

                if (node is DataClassPropertyNode) {
                    // Nested data class: add `copy` call recursively.
                    append(param.name)
                    if (node.nullable || param.hasNullableParents) {
                        append("?")
                    }
                    append(".")
                    append(copyData(node, params, parents + param))
                } else {
                    // Leaf property: use parameter directly.
                    append(param.name)
                }

                // Null safety if needed.
                if (param.hasNullableParents) {
                    append(" ?: ")
                    append(parents.lastOrNull()?.name ?: "this")
                    append(".")
                    append(node.name)
                }

                if (index < root.children.lastIndex) {
                    append(", ")
                }
            }
            append(")")
        }

    /**
     * Generates the method body containing the return statement.
     *
     * @param root The root property node
     * @param params All function parameters
     * @return The method body as a string
     */
    private fun methodBody(
        root: DataClassPropertyNode,
        params: List<Parameter>,
    ): String = "return ${copyData(root, params)}".prependIndent(INDENT)
}

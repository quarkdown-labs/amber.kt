package com.quarkdown.automerge.processors.nesteddata

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.quarkdown.automerge.processor.GenerationConstants.INDENT
import com.quarkdown.automerge.processor.dataclass.DataClassPropertyNode
import com.quarkdown.automerge.processor.dataclass.PropertyNode
import com.quarkdown.automerge.processor.dataclass.buildDataClassPropertiesTree
import com.quarkdown.automerge.processor.generator.ClassSourceGenerator

/** The name of the generated deep copy extension function. */
private const val FUNCTION_NAME = "deepCopy"

/** Separator used to flatten nested property names into parameter names. */
private const val PARAMETER_PROPERTY_SEPARATOR = "_"

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
            append(signatureLine(propertiesTree, params))
            appendLine(" {")
            appendLine(methodBody(propertiesTree, params))
            appendLine("}")
        }

    /**
     * A parameter in the generated function signature.
     *
     * @property name The flattened parameter name, where each parent parameter is separated by [PARAMETER_PROPERTY_SEPARATOR]
     *                (e.g., `first_second_third` for `first.second.third`)
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
            val paramName = (parents.asSequence().map { it.name } + node.name).joinToString(PARAMETER_PROPERTY_SEPARATOR)

            params +=
                Parameter(
                    name = paramName,
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
                    // a: A = this.a,
                    // a_b_c: C = a.b.c,
                    // a_b_c_d: D? = a.b.c.d,
                    append(INDENT)
                    append(param.name)
                    append(": ")
                    append(param.node.type)
                    if (param.node.nullable || param.hasNullableParents) append("?")
                    append(" = ")
                    if (param.parents.isEmpty()) append("this.")

                    // Any node after a nullable one needs a safe access operator.
                    var nullableChain = false
                    (param.parents + param.node).forEachIndexed { index, node ->
                        if (index > 0) {
                            if (nullableChain) append("?")
                            append(".")
                        }
                        append(node.name)
                        if (node.nullable) nullableChain = true
                    }
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
                    // Leaf property: use parameter directly, with null safety if needed.
                    append(param.name)
                    if (node.nullable || param.hasNullableParents) {
                        append(" ?: ")
                        parents.lastOrNull()?.let {
                            append(it.name)
                            append(".")
                        }
                        append(node.name)
                    }
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

package com.quarkdown.automerge.processors.nesteddata

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.quarkdown.automerge.processor.GenerationConstants.INDENT
import com.quarkdown.automerge.processor.dataclass.DataClassPropertyNode
import com.quarkdown.automerge.processor.dataclass.PropertyNode
import com.quarkdown.automerge.processor.dataclass.buildDataClassPropertiesTree
import com.quarkdown.automerge.processor.generator.ClassSourceGenerator

private const val FUNCTION_NAME = "deepCopy"

private const val PARAMETER_PROPERTY_SEPARATOR = "_"

/**
 * Generates an extension function that performs a deep copy of a data class by
 * exposing parameters for nested properties.
 *
 * The generator walks nested data-class properties and emits parameters for every
 * leaf and intermediate node (skipping the top-level names which are already
 * declared as top-level parameters). It then emits code that rebuilds the
 * nested data objects using copy(...) calls, applying null-safety where needed.
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

    private data class Parameter(
        val name: String,
        val node: PropertyNode,
        val parents: List<PropertyNode>,
    ) {
        val hasNullableParents: Boolean
            get() = parents.any { it.nullable }
    }

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

    private fun signatureLine(
        root: PropertyNode,
        params: List<Parameter>,
    ): String {
        val className = root.name

        val paramsSignature =
            buildString {
                params.forEach { param ->
                    append(INDENT)
                    append(param.name)
                    append(": ")
                    append(param.node.type)
                    if (param.node.nullable || param.hasNullableParents) append("?")
                    append(" = ")
                    if (param.parents.isEmpty()) append("this.")

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
                    append(param.name)
                    if (node.nullable || param.hasNullableParents) {
                        append("?")
                    }
                    append(".")
                    append(copyData(node, params, parents + param))
                } else {
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

    private fun methodBody(
        root: DataClassPropertyNode,
        params: List<Parameter>,
    ): String = "return ${copyData(root, params)}".prependIndent(INDENT)
}

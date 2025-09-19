package com.quarkdown.automerge.processors.nesteddata

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Nullability
import com.quarkdown.automerge.processor.GenerationConstants.INDENT
import com.quarkdown.automerge.processor.generator.ClassSourceGenerator
import com.quarkdown.automerge.processor.utils.dataClassProperties
import com.quarkdown.automerge.processor.utils.isDataClass

private const val FUNCTION_NAME = "deepCopy"

/**
 * Generates an extension function that performs a deep copy of a data class by
 * exposing parameters for nested properties.
 *
 * Example output (simplified):
 * fun Config.deepCopy(id: Int = this.id, app_theme: String = app.theme, ...): Config { ... }
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
            appendLine(signatureLine())
            appendLine(" {")
            appendLine(methodBody())
            appendLine("}")
        }

    private fun signatureLine(): String {
        val className = annotated.simpleName.asString()
        val topParams =
            annotated.dataClassProperties.joinToString(", ") { p ->
                val name = p.simpleName.asString()
                val resolved = p.type.resolve()
                val typeName =
                    qualifiedTypeName(
                        resolved.declaration as KSClassDeclaration,
                        resolved.nullability == Nullability.NULLABLE,
                    )
                "$name: $typeName = this.$name"
            }

        val nestedParams = buildNestedParams()
        val params = listOf(topParams, nestedParams).filter { it.isNotBlank() }.joinToString(", ")
        return "fun $className.$FUNCTION_NAME($params): $className"
    }

    private data class Param(
        val name: String,
        val type: String,
    )

    private fun KSClassDeclaration.children(): List<com.google.devtools.ksp.symbol.KSPropertyDeclaration> = this.dataClassProperties

    // Helper moved to class scope so it can be reused from signatureLine and buildNestedParams
    private fun qualifiedTypeName(
        decl: KSClassDeclaration,
        nullable: Boolean,
    ): String = decl.qualifiedName!!.asString() + if (nullable) "?" else ""

    /**
     * Build a comma-separated parameter list (as a String) describing all nested
     * parameters for the extension function. Each parameter is emitted as
     * "path_with_underscores: Type = ownerExpression".
     */
    private fun buildNestedParams(): String {
        val nestedParamsList = mutableListOf<Param>()

        fun visitNode(
            path: List<String>,
            decl: KSClassDeclaration,
            nullableChain: Boolean,
            ownerExpr: String,
        ) {
            // Add intermediate param for this node (except the root class itself; tests show only top-level direct props as top params)
            if (path.size > 1) { // skip adding the very first top-level data property name to avoid duplicates with top params
                val nodePropName = path.joinToString("_")
                nestedParamsList +=
                    Param(
                        name = nodePropName,
                        type = qualifiedTypeName(decl, nullableChain) + " = $ownerExpr",
                    )
            }

            // For each property
            for (prop in decl.children()) {
                val propName = prop.simpleName.asString()
                val propResolved = prop.type.resolve()
                val childIsData = propResolved.isDataClass
                val newPath = path + propName
                val safeOwnerExpr = ownerExpr + (if (nullableChain) "?" else "") + ".$propName"
                val nextNullable = nullableChain || (propResolved.nullability == Nullability.NULLABLE)
                if (childIsData) {
                    visitNode(
                        newPath,
                        propResolved.declaration as KSClassDeclaration,
                        nextNullable,
                        ownerExpr = safeOwnerExpr.removePrefix("this."),
                    )
                } else {
                    // Leaf param
                    val baseType = propResolved.declaration.qualifiedName!!.asString()
                    val leafType = baseType + if (nextNullable) "?" else ""
                    nestedParamsList +=
                        Param(
                            name = newPath.joinToString("_"),
                            type = "$leafType = $safeOwnerExpr",
                        )
                }
            }
        }

        // Start from each top-level data-class property
        for (p in annotated.dataClassProperties) {
            val pName = p.simpleName.asString()
            val resolved = p.type.resolve()
            val isData = resolved.isDataClass
            if (isData) {
                val nullable = resolved.nullability == Nullability.NULLABLE
                // For parameters, we must not duplicate top-level names (they are already top params).
                // So start path from the top-level name but skip emitting the first intermediate param.
                visitNode(
                    listOf(pName),
                    resolved.declaration as KSClassDeclaration,
                    nullable,
                    ownerExpr = pName,
                )
            }
        }

        return nestedParamsList.joinToString(", ") { "${it.name}: ${it.type}" }
    }

    private fun methodBody(): String =
        buildString {
            val props = annotated.dataClassProperties

            // For each top-level data-class property create a rebuilt variable, others are used directly
            for (p in props) {
                val pName = p.simpleName.asString()
                val resolved = p.type.resolve()
                val isData =
                    Modifier.DATA in resolved.declaration.modifiers && resolved.declaration is KSClassDeclaration
                if (isData) {
                    val newVar = "new" + pName.replaceFirstChar { it.uppercase() }

                    fun buildRebuildExpr(
                        path: List<String>,
                        decl: KSClassDeclaration,
                        nullableChain: Boolean,
                        ownerExpr: String,
                        indent: String = "",
                    ): String {
                        // Build copy arguments for children
                        val args =
                            decl.children().joinToString(", ") { child ->
                                val childName = child.simpleName.asString()
                                val childResolved = child.type.resolve()
                                val childIsData =
                                    Modifier.DATA in childResolved.declaration.modifiers && childResolved.declaration is KSClassDeclaration
                                val newPath = path + childName
                                if (childIsData) {
                                    val nextNullable =
                                        nullableChain || (childResolved.nullability == Nullability.NULLABLE)
                                    val childExpr =
                                        buildRebuildExpr(
                                            newPath,
                                            childResolved.declaration as KSClassDeclaration,
                                            nextNullable,
                                            ownerExpr + (if (nullableChain) "?" else "") + ".$childName",
                                            indent,
                                        )
                                    "$childName = $childExpr"
                                } else {
                                    val paramName = newPath.joinToString("_")
                                    if (nullableChain) "$childName = ($paramName ?: $ownerExpr.$childName)" else "$childName = $paramName"
                                }
                            }
                        val nullSafe = if (nullableChain) "?" else ""
                        val base = "$ownerExpr$nullSafe.copy($args)"
                        return if (nullableChain) "$base ?: $ownerExpr" else base
                    }

                    val decl = resolved.declaration as KSClassDeclaration
                    val nullable = resolved.nullability == Nullability.NULLABLE
                    val expr = buildRebuildExpr(listOf(pName), decl, nullable, ownerExpr = pName)
                    appendLine("val $newVar = $expr")
                }
            }

            // Build final return
            val namedTop =
                props.joinToString(", ") { p ->
                    val n = p.simpleName.asString()
                    val r = p.type.resolve()
                    val isData = Modifier.DATA in r.declaration.modifiers && r.declaration is KSClassDeclaration
                    if (isData) {
                        val newVar = "new" + n.replaceFirstChar { it.uppercase() }
                        "$n = $newVar"
                    } else {
                        "$n = $n"
                    }
                }
            append("return ${annotated.simpleName.asString()}($namedTop)")
        }.prependIndent(INDENT)
}

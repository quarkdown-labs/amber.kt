package com.quarkdown.amber.processors.exportresource

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.quarkdown.amber.processor.GenerationConstants.INDENT
import com.quarkdown.amber.processor.generator.ClassSourceGenerator
import com.quarkdown.amber.processor.utils.KDocUtils
import com.quarkdown.amber.processor.utils.arguments
import com.quarkdown.amber.processor.utils.declaration
import com.quarkdown.amber.processor.utils.enclosingDeclarations
import com.quarkdown.amber.processor.utils.packageScopedName
import com.quarkdown.amber.processor.utils.whereClause

/** Type of the generated properties. */
private const val PROPERTY_TYPE: String = "kotlin.String"

/**
 * Generates one extension property per resource exported by a
 * [com.quarkdown.amber.annotations.ExportResource]-annotated class, each returning the content
 * the resource had at compile time.
 *
 * For `@ExportResource("/banner.txt") object Assets` produces:
 * ```
 * val Assets.banner: kotlin.String
 *     get() = "..."
 * ```
 */
class ExportResourceSourceGenerator(
    environment: SymbolProcessorEnvironment,
    annotated: KSClassDeclaration,
    private val resources: List<ExportedResource>,
) : ClassSourceGenerator(environment, annotated) {
    override val fileNameSuffix: String
        get() = "ExportResource"

    override fun generateSourceBody(annotated: KSClassDeclaration): String {
        // An inner class of a generic declaration would need the enclosing type arguments
        // (`Outer<T>.Inner`), which the extension cannot bind to a resource-only property.
        require(Modifier.INNER !in annotated.modifiers || annotated.enclosingDeclarations.none { it.typeParameters.isNotEmpty() }) {
            "${annotated.qualifiedName?.asString()} is an inner class of a generic declaration, which cannot be a receiver here"
        }

        val typeParameters = annotated.typeParameters
        val receiver = "${typeParameters.declaration}${annotated.packageScopedName}${typeParameters.arguments}"
        return resources.joinToString("\n") { it.toProperty(receiver, typeParameters.whereClause) }
    }

    /** Renders this resource as an extension property on [receiver]. */
    private fun ExportedResource.toProperty(
        receiver: String,
        whereClause: String,
    ): String =
        buildString {
            appendLine(KDocUtils.generate("Content of resource `$path`, embedded at compile time."))
            appendLine("val $receiver.$propertyName: $PROPERTY_TYPE$whereClause")
            appendLine("${INDENT}get() = \"${content.escapedForLiteral}\"")
        }
}

/** This text, escaped to be safely embedded in a Kotlin string literal. */
private val String.escapedForLiteral: String
    get() =
        buildString {
            for (char in this@escapedForLiteral) {
                when {
                    char == '\\' -> append("\\\\")
                    char == '"' -> append("\\\"")
                    char == '$' -> append("\\$")
                    char == '\n' -> append("\\n")
                    char == '\r' -> append("\\r")
                    char == '\t' -> append("\\t")
                    char.isISOControl() -> append("\\u%04x".format(char.code))
                    else -> append(char)
                }
            }
        }

package com.quarkdown.automerge.processors.mergeable

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Nullability
import com.quarkdown.automerge.processor.GenerationConstants.INDENT
import com.quarkdown.automerge.processor.generator.ClassSourceGenerator
import com.quarkdown.automerge.processor.utils.dataClassProperties

/** Name of the generated merge function. */
private const val MERGE_FUNCTION_NAME: String = "merge"

/** Name of the parameter representing the other instance to merge with. */
private const val MERGE_PARAMETER_NAME: String = "other"

/**
 * Generates the merge extension function for a [com.quarkdown.automerge.annotations.Mergeable] data class.
 *
 * For a data class MyType, this generator produces a file named MyType_Mergeable.kt that contains
 * an extension function fun MyType.merge(other: MyType?): MyType which returns a copy of the
 * receiver where every nullable property falls back to the value from other when the receiver's
 * value is null. Non-nullable properties are copied as-is from the receiver.
 */
class MergeableSourceGenerator(
    environment: SymbolProcessorEnvironment,
    annotated: KSClassDeclaration,
) : ClassSourceGenerator(environment, annotated) {
    override val fileNameSuffix: String
        get() = "Mergeable"

    override fun generateSourceBody(annotated: KSClassDeclaration) =
        buildString {
            append(signatureLine())
            appendLine(" {")
            appendLine(methodBody())
            appendLine("}")
        }

    /** Build the function signature line for the generated extension. */
    private fun signatureLine(): String {
        val className = annotated.simpleName.asString()
        return "fun $className.$MERGE_FUNCTION_NAME($MERGE_PARAMETER_NAME: $className?): $className"
    }

    /** Build the function body implementing the merge behavior. */
    private fun methodBody(): String =
        buildString {
            val properties = annotated.dataClassProperties

            appendLine("if ($MERGE_PARAMETER_NAME == null) return copy()")
            append("return copy(\n")
            properties.forEach {
                append(INDENT)
                append(propertyCopyLine(it))
            }
            append(")")
        }.prependIndent(INDENT)

    /** Produce a single named argument for copy() according to nullability policy. */
    private fun propertyCopyLine(property: KSPropertyDeclaration): String {
        val name = property.simpleName.asString()
        val isNullable = property.type.resolve().nullability == Nullability.NULLABLE
        val value = if (isNullable) "this.$name ?: $MERGE_PARAMETER_NAME.$name" else "this.$name"
        return "$name = $value,\n"
    }
}

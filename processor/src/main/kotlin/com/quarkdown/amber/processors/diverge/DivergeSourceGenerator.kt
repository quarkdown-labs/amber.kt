package com.quarkdown.amber.processors.diverge

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.quarkdown.amber.processor.GenerationConstants.INDENT
import com.quarkdown.amber.processor.generator.ClassSourceGenerator
import com.quarkdown.amber.processor.utils.formattedName

/** Name of the generated extension function. */
private const val DIVERGE_FUNCTION_NAME: String = "diverge"

/**
 * Generates the diverge extension for a class with one or more `@Diverge`-marked
 * primary-constructor parameters.
 *
 * For `class Person(val name: String, @Diverge val age: Int)` produces:
 * ```
 * fun Person.diverge(
 *     age: kotlin.Int = this.age,
 * ): Person = Person(
 *     name = this.name,
 *     age = age,
 * )
 * ```
 */
class DivergeSourceGenerator(
    environment: SymbolProcessorEnvironment,
    annotated: KSClassDeclaration,
    private val markedParams: Set<String>,
) : ClassSourceGenerator(environment, annotated) {
    override val fileNameSuffix: String
        get() = "Diverge"

    override fun generateSourceBody(annotated: KSClassDeclaration): String {
        val ctor = annotated.primaryConstructor
            ?: error("class ${annotated.qualifiedName?.asString()} has no primary constructor")
        val ctorParams = ctor.parameters
        val nonProperty = ctorParams.filterNot { it.isVal || it.isVar }
        require(nonProperty.isEmpty()) {
            "all primary-constructor parameters of ${annotated.qualifiedName?.asString()} must be 'val' or 'var' " +
                "(non-property params: ${nonProperty.mapNotNull { it.name?.asString() }})"
        }

        val className = annotated.simpleName.asString()
        val signature = ctorParams
            .filter { it.name!!.asString() in markedParams }
            .joinToString(separator = "") { it.signatureLine() }
        val ctorArgs = ctorParams.joinToString(separator = "") { it.callArgLine() }

        return buildString {
            appendLine("fun $className.$DIVERGE_FUNCTION_NAME(")
            append(signature)
            appendLine("): $className = $className(")
            append(ctorArgs)
            append(")")
        }
    }

    private fun KSValueParameter.signatureLine(): String {
        val name = name!!.asString()
        return "$INDENT$name: ${type.resolve().renderNullable()} = this.$name,\n"
    }

    private fun KSValueParameter.callArgLine(): String {
        val name = name!!.asString()
        val rhs = if (name in markedParams) name else "this.$name"
        return "$INDENT$name = $rhs,\n"
    }
}

private fun KSType.renderNullable(): String = if (isMarkedNullable) "$formattedName?" else formattedName

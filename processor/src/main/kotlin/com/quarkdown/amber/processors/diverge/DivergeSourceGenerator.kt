package com.quarkdown.amber.processors.diverge

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSValueParameter
import com.quarkdown.amber.processor.GenerationConstants.INDENT
import com.quarkdown.amber.processor.generator.ClassSourceGenerator

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
        val typeParams = annotated.typeParameters
        val typeParamsDecl = typeParams.joinNames(prefix = "<", postfix = "> ")
        val typeArgs = typeParams.joinNames(prefix = "<", postfix = ">")
        val explicitBounds = typeParams.flatMap { tp ->
            tp.bounds.map { tp.name.asString() to it.resolve() }.filterNot { (_, b) -> b.isAnyDefaultBound() }
        }
        val whereClause = if (explicitBounds.isEmpty()) "" else
            explicitBounds.joinToString(", ", prefix = " where ") { (n, b) -> "$n : ${b.renderTypeUse()}" }
        val receiver = "$className$typeArgs"

        val signature = ctorParams
            .filter { it.name!!.asString() in markedParams }
            .joinToString(separator = "") { it.signatureLine() }
        val ctorArgs = ctorParams.joinToString(separator = "") { it.callArgLine() }

        return buildString {
            appendLine("fun $typeParamsDecl$receiver.$DIVERGE_FUNCTION_NAME(")
            append(signature)
            appendLine("): $receiver$whereClause = $className$typeArgs(")
            append(ctorArgs)
            append(")")
        }
    }

    private fun List<KSTypeParameter>.joinNames(prefix: String, postfix: String): String =
        if (isEmpty()) "" else joinToString(", ", prefix, postfix) { it.name.asString() }

    private fun KSValueParameter.signatureLine(): String {
        val name = name!!.asString()
        return "$INDENT$name: ${type.resolve().renderTypeUse()} = this.$name,\n"
    }

    private fun KSValueParameter.callArgLine(): String {
        val name = name!!.asString()
        val rhs = if (name in markedParams) name else "this.$name"
        return "$INDENT$name = $rhs,\n"
    }
}

/** Render [this] as Kotlin source: FQN for declared types, short name for type parameters, recursing into generic args. */
private fun KSType.renderTypeUse(): String {
    val decl = declaration
    val base = if (decl is KSTypeParameter) decl.name.asString()
        else decl.qualifiedName?.asString() ?: decl.simpleName.asString()
    val rendered = if (arguments.isEmpty()) base
        else "$base<${arguments.joinToString(", ") { it.type?.resolve()?.renderTypeUse() ?: "*" }}>"
    return if (isMarkedNullable) "$rendered?" else rendered
}

/** True if this is the implicit `kotlin.Any?` upper bound KSP adds to unbounded type parameters. */
private fun KSType.isAnyDefaultBound(): Boolean =
    isMarkedNullable && declaration.qualifiedName?.asString() == "kotlin.Any"

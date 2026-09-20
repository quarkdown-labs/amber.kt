package com.quarkdown.amber.processor.utils

import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeParameter

/** Type parameter declaration of an extension, e.g. `<T, R> `, including the trailing space. */
val List<KSTypeParameter>.declaration: String
    get() = joinNames(prefix = "<", postfix = "> ")

/** Type arguments to apply to the receiver type, e.g. `<T, R>`. */
val List<KSTypeParameter>.arguments: String
    get() = joinNames(prefix = "<", postfix = ">")

/**
 * `where` clause carrying over the explicit upper bounds of the type parameters, e.g.
 * ` where T : kotlin.Comparable<T>`, including the leading space.
 *
 * The implicit `kotlin.Any?` bound KSP reports for unbounded parameters is omitted.
 */
val List<KSTypeParameter>.whereClause: String
    get() {
        val bounds =
            flatMap { parameter ->
                parameter.bounds
                    .map { parameter.name.asString() to it.resolve() }
                    .filterNot { (_, bound) -> bound.isImplicitBound }
            }

        if (bounds.isEmpty()) return ""
        return bounds.joinToString(", ", prefix = " where ") { (name, bound) -> "$name : ${bound.typeUseName}" }
    }

private fun List<KSTypeParameter>.joinNames(
    prefix: String,
    postfix: String,
): String = if (isEmpty()) "" else joinToString(", ", prefix, postfix) { it.name.asString() }

/** Whether this is the implicit `kotlin.Any?` upper bound KSP reports for unbounded type parameters. */
private val KSType.isImplicitBound: Boolean
    get() = isMarkedNullable && declaration.qualifiedName?.asString() == "kotlin.Any"

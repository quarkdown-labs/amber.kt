package com.quarkdown.amber.processors.diverge

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.quarkdown.amber.annotations.Diverge
import com.quarkdown.amber.processor.AnnotationProcessorBase

/**
 * KSP processor for [Diverge].
 *
 * Normalises every annotated symbol into a set of marked primary-constructor parameter
 * names per class:
 * - `@Diverge` on a value parameter -> that parameter is marked.
 * - `@Diverge` on the primary constructor -> all its parameters are marked.
 * - `@Diverge` on the class -> all primary-constructor parameters are marked.
 *
 * For each class with at least one mark, generates `<ClassName>_Diverge.kt`.
 */
class DivergeProcessor(
    environment: SymbolProcessorEnvironment,
) : AnnotationProcessorBase(environment, Diverge::class) {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val (valid, deferred) = partitionSymbols(resolver)

        val markedParamsByClass = LinkedHashMap<KSClassDeclaration, MutableSet<String>>()
        for (symbol in valid) {
            val target = guarded(symbol) { resolveTarget(symbol) } ?: continue
            markedParamsByClass.getOrPut(target.first) { linkedSetOf() } += target.second
        }

        for ((cls, params) in markedParamsByClass) {
            guarded(cls) { emit(DivergeSourceGenerator(environment, cls, params)) }
        }

        return deferred
    }

    private fun resolveTarget(symbol: KSAnnotated): Pair<KSClassDeclaration, Set<String>> =
        when (symbol) {
            is KSValueParameter -> {
                val ctor = symbol.parent as? KSFunctionDeclaration
                    ?: error("a value parameter must live inside a primary constructor")
                ctor.requirePrimaryConstructorClass() to setOf(symbol.name!!.asString())
            }
            is KSFunctionDeclaration -> {
                symbol.requirePrimaryConstructorClass() to symbol.parameters.allParamNames()
            }
            is KSClassDeclaration -> {
                val ctor = symbol.primaryConstructor
                    ?: error("class ${symbol.qualifiedName?.asString()} has no primary constructor")
                symbol to ctor.parameters.allParamNames()
            }
            else -> error("unsupported annotation target: ${symbol::class.simpleName}")
        }

    private fun KSFunctionDeclaration.requirePrimaryConstructorClass(): KSClassDeclaration {
        val cls = parentDeclaration as? KSClassDeclaration
            ?: error("a constructor must be declared inside a class")
        require(this == cls.primaryConstructor) {
            "@Diverge must target the primary constructor of ${cls.qualifiedName?.asString()}"
        }
        return cls
    }

    private fun List<KSValueParameter>.allParamNames(): Set<String> =
        mapNotNull { it.name?.asString() }.toSet()
}

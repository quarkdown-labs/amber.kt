package com.quarkdown.amber.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.validate
import com.quarkdown.amber.processor.generator.SourceGenerator
import com.quarkdown.amber.processor.generator.writeFile
import kotlin.reflect.KClass

/**
 * Shared infrastructure for KSP processors focused on a single annotation type.
 *
 * Sits between [SymbolProcessor] and the more opinionated [AnnotationProcessor]: it owns
 * the annotation's FQN/name lookup, the logger, and the helpers that subclasses use to
 * partition symbols by KSP's [validate] gate, run risky work with structured error
 * reporting, and write a generator's output.
 *
 * Subclasses implement [process] freely — typical shapes are one-symbol-per-file
 * (see [AnnotationProcessor]) or many-symbols-grouped-by-class.
 */
abstract class AnnotationProcessorBase(
    protected val environment: SymbolProcessorEnvironment,
    annotation: KClass<out Annotation>,
) : SymbolProcessor {
    protected val logger: KSPLogger = environment.logger

    private val annotationName: String = annotation.java.simpleName
    private val annotationFqn: String = annotation.java.run { "$packageName.$simpleName" }

    /**
     * Partition this round's annotated symbols into (valid, deferred). Deferred symbols
     * should be returned from [process] so KSP revisits them next round.
     */
    protected fun partitionSymbols(resolver: Resolver): Pair<List<KSAnnotated>, List<KSAnnotated>> =
        resolver.getSymbolsWithAnnotation(annotationFqn).partition { it.validate() }

    /**
     * Run [block] and return its result, or null if it throws. Exceptions are reported
     * via [KSPLogger.error] anchored on [node] (typically the symbol being processed) so
     * KSP surfaces them as compilation errors rather than crashing the round.
     */
    protected fun <T> guarded(node: KSNode?, block: () -> T): T? =
        try {
            block()
        } catch (e: Exception) {
            logger.error("Failed to process @$annotationName: ${e.message}", node)
            null
        }

    /** Write the generator's source to its destination file. */
    protected fun emit(generator: SourceGenerator<*>) {
        generator.writeFile(generator.generateSource())
    }
}

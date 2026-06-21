package com.quarkdown.amber.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.quarkdown.amber.processor.generator.SourceGenerator
import kotlin.reflect.KClass

/**
 * KSP processor for the simple "one annotated symbol -> one generated file" shape.
 *
 * For each symbol carrying [annotation], [validate] coerces it to the expected type [T]
 * (or throws to abort that one symbol), then [generatorProvider] builds the per-symbol
 * source generator. Errors are reported via KSP's logger; KSP-deferred symbols are
 * returned for retry in a later round.
 *
 * Use this when each annotated symbol stands on its own. For processors that need to
 * group multiple symbols into one file, extend [AnnotationProcessorBase] directly.
 */
abstract class AnnotationProcessor<T : KSAnnotated>(
    environment: SymbolProcessorEnvironment,
    annotation: KClass<out Annotation>,
    private val generatorProvider: (T) -> SourceGenerator<T>,
) : AnnotationProcessorBase(environment, annotation) {
    final override fun process(resolver: Resolver): List<KSAnnotated> {
        val (valid, deferred) = partitionSymbols(resolver)
        for (symbol in valid) {
            val annotated = guarded(symbol) { validate(symbol) } ?: continue
            guarded(symbol) { emit(generatorProvider(annotated)) }
        }
        return deferred
    }

    /** Ensure [annotated] is of the expected type and shape; return the casted/validated value. */
    protected abstract fun validate(annotated: KSAnnotated): T
}

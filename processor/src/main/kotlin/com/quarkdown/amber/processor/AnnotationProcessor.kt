package com.quarkdown.amber.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.validate
import com.quarkdown.amber.processor.generator.SourceGenerator
import com.quarkdown.amber.processor.generator.writeFile
import kotlin.reflect.KClass

/**
 * Base KSP SymbolProcessor for annotations handled by this module.
 *
 * This processor locates symbols annotated with the provided [annotation], validates each symbol
 * via [validate], and delegates source generation to a [SourceGenerator] created by
 * [generatorProvider]. Implementations only need to provide the validation logic and the concrete
 * generator.
 *
 * @param T The specific KSP annotated type this processor accepts (e.g., KSClassDeclaration).
 * @param environment KSP-provided environment with logger and code generator.
 * @param annotation The annotation class to search for.
 * @param generatorProvider Factory that builds a [SourceGenerator] for a validated symbol.
 */
abstract class AnnotationProcessor<T : KSAnnotated>(
    environment: SymbolProcessorEnvironment,
    annotation: KClass<out Annotation>,
    private val generatorProvider: (T) -> SourceGenerator<T>,
) : SymbolProcessor {
    /** Logger for reporting errors and info during processing. */
    private val logger: KSPLogger = environment.logger

    /** Fully-qualified name of the processed annotation. */
    private val annotationFullyQualifiedName: String = annotation.java.run { "$packageName.$simpleName" }

    /** Simple (unqualified) name of the processed annotation. */
    private val annotationSimpleName: String = annotation.java.simpleName

    /**
     * Entry point called by KSP in each round.
     *
     * Returns the list of symbols that couldn't be validated in this round so KSP can revisit
     * them later, as per its standard contract.
     */
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(annotationFullyQualifiedName)
        val unableToProcess = mutableListOf<KSAnnotated>()

        for (symbol in symbols) {
            if (!symbol.validate()) {
                unableToProcess.add(symbol)
                continue
            }

            val annotated: T =
                try {
                    this.validate(symbol)
                } catch (e: Exception) {
                    logger.error("Failed to process @$annotationSimpleName: ${e.message}", symbol)
                    continue
                }

            val generator = generatorProvider(annotated)
            val source = generator.generateSource()
            generator.writeFile(source)
        }

        return unableToProcess
    }

    /** Ensure [annotated] is of the expected type and shape; return the casted/validated value. */
    protected abstract fun validate(annotated: KSAnnotated): T
}

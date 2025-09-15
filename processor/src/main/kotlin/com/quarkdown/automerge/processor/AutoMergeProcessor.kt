package com.quarkdown.automerge.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.validate

/**
 * KSP SymbolProcessor that locates classes annotated with `@AutoMerge` and generates a
 * `merge` extension function for each valid data class.
 *
 * Readability and scalability improvements:
 * - Generation logic is delegated to [MergeExtensionGenerator].
 * - Constants centralized in [GenerationConstants].
 * - Clear KDoc and messages, behavior unchanged.
 */
class AutoMergeProcessor(
    environment: SymbolProcessorEnvironment,
) : SymbolProcessor {
    private val logger: KSPLogger = environment.logger
    private val generator = MergeExtensionGenerator(environment.codeGenerator)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(GenerationConstants.ANNOTATION_FQN)
        println(symbols.toList().map { it })
        val unableToProcess = mutableListOf<KSAnnotated>()
        for (symbol in symbols) {
            if (!symbol.validate()) {
                unableToProcess.add(symbol)
                continue
            }

            val classDeclaration = symbol as? KSClassDeclaration

            if (classDeclaration == null) {
                logger.warn("@${GenerationConstants.ANNOTATION_SIMPLE_NAME} is applicable to classes only")
                continue
            }

            if (classDeclaration.classKind != ClassKind.CLASS || !classDeclaration.modifiers.contains(Modifier.DATA)) {
                logger.error("@${GenerationConstants.ANNOTATION_SIMPLE_NAME} can only be used on data classes", classDeclaration)
                continue
            }

            generator.generate(classDeclaration)
        }
        return unableToProcess
    }
}

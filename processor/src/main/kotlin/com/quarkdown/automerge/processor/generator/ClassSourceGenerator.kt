package com.quarkdown.automerge.processor.generator

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.quarkdown.automerge.processor.GenerationConstants

/**
 * Base implementation of SourceGenerator for generators that produce source for a class.
 *
 * It provides common header/boilerplate and file naming logic and delegates the class-specific
 * body generation to subclasses through [generateSourceBody].
 */
abstract class ClassSourceGenerator(
    override val environment: SymbolProcessorEnvironment,
    override val annotated: KSClassDeclaration,
) : SourceGenerator<KSClassDeclaration> {
    /** Suffix appended to the source file name, after the class name. */
    abstract val fileNameSuffix: String

    /** Package name of the class being processed (used for the output too). */
    override val packageName: String
        get() = annotated.packageName.asString()

    /** Output file name without extension (e.g., MyClass_Mergeable). */
    override val fileName: String
        get() = "${annotated.simpleName.asString()}_$fileNameSuffix"

    /**
     * Produces the full source text including file-level suppressions, package line,
     * generated header, and the body returned by [generateSourceBody].
     */
    final override fun generateSource(): String =
        buildString {
            appendLine(GenerationConstants.SUPPRESS_ANNOTATION)
            appendLine()
            appendLine("package ${annotated.packageName.asString()}")
            appendLine()
            appendLine(GenerationConstants.GENERATED_HEADER)
            appendLine()
            appendLine(generateSourceBody(annotated))
        }

    /** Generate only the class-specific body of the file. */
    protected abstract fun generateSourceBody(annotated: KSClassDeclaration): String
}

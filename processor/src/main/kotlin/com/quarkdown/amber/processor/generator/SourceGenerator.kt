package com.quarkdown.amber.processor.generator

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated

/**
 * Generic contract for source generators used by the KSP processors in this module.
 *
 * A SourceGenerator takes a KSP annotated element and produces a Kotlin source file text
 * that can be written to disk by the processor. Implementations are free to choose the
 * concrete element type they operate on (classes, functions, etc.).
 *
 * @param T The KSP element type this generator knows how to handle.
 */
interface SourceGenerator<T : KSAnnotated> {
    /** KSP environment giving access to logging and the code generator. */
    val environment: SymbolProcessorEnvironment

    /** The annotated KSP element this source will be generated for. */
    val annotated: T

    /** Target package of the generated source file. */
    val packageName: String

    /** File name (without extension) of the generated Kotlin file. */
    val fileName: String

    /**
     * Produce the full Kotlin source text to be written to the destination file.
     */
    fun generateSource(): String
}

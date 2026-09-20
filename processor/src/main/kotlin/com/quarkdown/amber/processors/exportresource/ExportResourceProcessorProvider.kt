package com.quarkdown.amber.processors.exportresource

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * Registers [ExportResourceProcessor] with KSP.
 */
class ExportResourceProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = ExportResourceProcessor(environment)
}

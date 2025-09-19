package com.quarkdown.automerge.processors.nesteddata

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/** Registers [NestedDataProcessor] with KSP. */
class NestedDataProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = NestedDataProcessor(environment)
}

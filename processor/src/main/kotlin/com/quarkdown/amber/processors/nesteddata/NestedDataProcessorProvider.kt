package com.quarkdown.amber.processors.nesteddata

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * Service provider that registers [NestedDataProcessor] with KSP.
 */
class NestedDataProcessorProvider : SymbolProcessorProvider {
    /**
     * Creates a new instance of [NestedDataProcessor].
     *
     * @param environment The KSP processing environment containing loggers, file generators,
     *                   and other utilities needed for source code generation
     * @return A configured [NestedDataProcessor] instance
     */
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = NestedDataProcessor(environment)
}

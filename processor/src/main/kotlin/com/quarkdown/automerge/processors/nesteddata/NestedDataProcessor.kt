package com.quarkdown.automerge.processors.nesteddata

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.quarkdown.automerge.annotations.NestedData
import com.quarkdown.automerge.processor.AnnotationProcessor
import com.quarkdown.automerge.processor.dataclass.isDataClass

/**
 * KSP processor that handles classes annotated with @NestedData.
 */
class NestedDataProcessor(
    environment: SymbolProcessorEnvironment,
) : AnnotationProcessor<KSClassDeclaration>(
        environment,
        annotation = NestedData::class,
        generatorProvider = { NestedDataSourceGenerator(environment, it) },
    ) {
    override fun validate(annotated: KSAnnotated): KSClassDeclaration {
        require(annotated is KSClassDeclaration && annotated.isDataClass) { "Only applicable to data classes" }
        return annotated
    }
}

package com.quarkdown.automerge.processors.mergeable

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.quarkdown.automerge.annotations.Mergeable
import com.quarkdown.automerge.processor.AnnotationProcessor
import com.quarkdown.automerge.processor.dataclass.isDataClass

/**
 * KSP processor that handles classes annotated with [Mergeable].
 *
 * It accepts only Kotlin data classes and generates extension utilities via MergeableSourceGenerator.
 */
class MergeableProcessor(
    environment: SymbolProcessorEnvironment,
) : AnnotationProcessor<KSClassDeclaration>(
        environment,
        annotation = Mergeable::class,
        generatorProvider = { MergeableSourceGenerator(environment, it) },
    ) {
    override fun validate(annotated: KSAnnotated): KSClassDeclaration {
        require(annotated is KSClassDeclaration && annotated.isDataClass) { "Only applicable to data classes" }
        return annotated
    }
}

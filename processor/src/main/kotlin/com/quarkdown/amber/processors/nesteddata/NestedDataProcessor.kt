package com.quarkdown.amber.processors.nesteddata

import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.quarkdown.amber.annotations.NestedData
import com.quarkdown.amber.processor.AnnotationProcessor
import com.quarkdown.amber.processor.dataclass.isDataClass

/**
 * KSP processor that handles classes annotated with [NestedData].
 *
 * This processor generates deep copy extension functions for data classes, enabling
 * control over nested property modifications. The processor validates that the annotated element
 * is a data class and delegates source generation to [NestedDataSourceGenerator].
 *
 * For example, the `deepCopy` function allows modifying nested properties directly:
 *
 * ```
 * @NestedData
 * data class Person(val name: String, val address: Address)
 *
 * data class Address(val street: String, val city: City)
 * data class City(val name: String, val zipCode: String)
 * ```
 *
 * The generated `deepCopy` function can be used as follows:
 *
 * ```
 * val person = Person(...)
 * val updatedPerson = person.deepCopy(address_city_name = "New City")
 * ```
 *
 * @param environment The KSP processing environment
 */
class NestedDataProcessor(
    environment: SymbolProcessorEnvironment,
) : AnnotationProcessor<KSClassDeclaration>(
        environment,
        annotation = NestedData::class,
        generatorProvider = { NestedDataSourceGenerator(environment, it) },
    ) {
    /**
     * Validates that the annotated element is a data class.
     *
     * @param annotated The annotated symbol to validate
     * @return The validated class declaration
     */
    override fun validate(annotated: KSAnnotated): KSClassDeclaration {
        require(annotated is KSClassDeclaration && annotated.isDataClass) { "Only applicable to data classes" }
        return annotated
    }
}

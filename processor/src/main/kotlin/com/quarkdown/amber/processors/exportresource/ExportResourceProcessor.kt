package com.quarkdown.amber.processors.exportresource

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.quarkdown.amber.annotations.ExportResource
import com.quarkdown.amber.processor.AnnotationProcessorBase

/** Name of the annotation argument holding the resource path. */
private const val PATH_ARGUMENT: String = "path"

/** Name of the annotation argument holding the generated property name. */
private const val NAME_ARGUMENT: String = "name"

/**
 * KSP processor for [ExportResource].
 *
 * Since the annotation is repeatable, every occurrence on a class is collected into a single
 * `<ClassName>_ExportResource.kt` file, where each resource is read from disk at compile time
 * and exposed as an extension property of the class.
 */
class ExportResourceProcessor(
    environment: SymbolProcessorEnvironment,
) : AnnotationProcessorBase(environment, ExportResource::class) {
    private val locator = ResourceLocator(environment.options)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val (valid, deferred) = partitionSymbols(resolver)

        // A class annotated more than once is reported once per occurrence.
        for (symbol in valid.distinct()) {
            val cls = guarded(symbol) { symbol.asClassDeclaration() } ?: continue
            val resources = guarded(cls) { cls.exportedResources() } ?: continue
            emitOnce(cls) { ExportResourceSourceGenerator(environment, cls, resources) }
        }

        return deferred
    }

    private fun KSAnnotated.asClassDeclaration(): KSClassDeclaration {
        require(this is KSClassDeclaration) { "only applicable to classes and objects" }
        // The generated properties extend the annotated declaration, which must be nameable from its package.
        requireNotNull(qualifiedName) { "${simpleName.asString()} is local or anonymous, hence cannot be extended" }
        return this
    }

    /** Resources this class exports, one per [ExportResource] occurrence, read from disk. */
    private fun KSClassDeclaration.exportedResources(): List<ExportedResource> {
        val resources =
            annotations
                .filter { it.annotationType.resolve().declaration.qualifiedName?.asString() == annotationFqn }
                .map { it.toExportedResource(owner = this) }
                .toList()

        val duplicates =
            resources
                .groupingBy { it.propertyName }
                .eachCount()
                .filterValues { it > 1 }
                .keys

        require(duplicates.isEmpty()) {
            "${qualifiedName?.asString()} exports multiple resources under the same name: ${duplicates.joinToString()}"
        }
        return resources
    }

    /** Reads the resource this annotation points to, relative to [owner]'s package if the path is not absolute. */
    private fun KSAnnotation.toExportedResource(owner: KSClassDeclaration): ExportedResource {
        val path = stringArgument(PATH_ARGUMENT) ?: error("a resource path is required")
        val file = locator.locate(path, owner.packageName.asString())

        return ExportedResource(
            propertyName = stringArgument(NAME_ARGUMENT) ?: path.inferredPropertyName,
            path = path,
            content = file.readText(),
        )
    }

    /** Value of the [name] argument of this annotation, or null if absent or blank. */
    private fun KSAnnotation.stringArgument(name: String): String? =
        arguments
            .firstOrNull { it.name?.asString() == name }
            ?.value
            ?.let { it as? String }
            ?.takeIf { it.isNotBlank() }
}

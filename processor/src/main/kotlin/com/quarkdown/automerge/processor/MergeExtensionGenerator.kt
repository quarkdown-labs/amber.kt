package com.quarkdown.automerge.processor

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.Nullability
import com.quarkdown.automerge.processor.GenerationConstants.INDENT
import com.quarkdown.automerge.processor.GenerationConstants.MERGE_FUNCTION_NAME
import com.quarkdown.automerge.processor.GenerationConstants.MERGE_PARAMETER_NAME
import java.io.OutputStreamWriter

/**
 * Generates the Kotlin extension function `<T>.merge(fallback: T): T` for a given data class.
 *
 * Behavior: for each primary-constructor property, if the type is nullable, the generated merge
 * uses `this.prop ?: fallback.prop`; for non-nullable properties it keeps `this.prop`.
 *
 * This mirrors the previous single-file implementation; only structure and documentation were improved.
 */
internal class MergeExtensionGenerator(
    private val codeGenerator: CodeGenerator,
) {
    /**
     * Emits the generated file for the provided data class declaration.
     * The file is named `<ClassName>_AutoMerge.kt` in the same package as the class.
     */
    fun generate(classDecl: KSClassDeclaration) {
        val pkg = classDecl.packageName.asString()
        val className = classDecl.simpleName.asString()
        val fileName = "${className}_AutoMerge"

        val dataProps = dataConstructorPropertiesOf(classDecl)

        val content =
            buildFileContent(
                pkg = pkg,
                className = className,
                properties = dataProps,
            )

        writeFile(
            pkg = pkg,
            fileName = fileName,
            originating = classDecl,
            content = content,
        )
    }

    /** Returns the declared properties that belong to the primary constructor. */
    private fun dataConstructorPropertiesOf(classDeclaration: KSClassDeclaration) =
        classDeclaration
            .getDeclaredProperties()
            .filter { p ->
                val ctorNames =
                    classDeclaration.primaryConstructor
                        ?.parameters
                        ?.map { it.name?.asString() }
                        ?.toSet() ?: emptySet()
                p.simpleName.asString() in ctorNames
            }.toList()

    private fun buildFileContent(
        pkg: String,
        className: String,
        properties: List<KSPropertyDeclaration>,
    ): String =
        buildString {
            appendLine("package $pkg")
            appendLine()
            appendLine(GenerationConstants.GENERATED_HEADER)
            appendLine(suppressAnnotation())
            append(signatureLine(className))
            append(methodBody(properties))
        }

    private fun suppressAnnotation(): String = "@Suppress(\"RedundantNullableReturnType\", \"UNUSED_PARAMETER\")"

    private fun signatureLine(className: String): String = "fun $className.$MERGE_FUNCTION_NAME(fallback: $className): $className =\n"

    private fun methodBody(properties: List<KSPropertyDeclaration>): String =
        buildString {
            append("this.copy(\n")
            properties.forEach {
                append(INDENT)
                append(propertyCopyLine(it))
            }
            append(")\n")
        }.prependIndent(INDENT)

    private fun propertyCopyLine(prop: KSPropertyDeclaration): String {
        val name = prop.simpleName.asString()
        val isNullable = prop.type.resolve().nullability == Nullability.NULLABLE
        val rhs = if (isNullable) "this.$name ?: $MERGE_PARAMETER_NAME.$name" else "this.$name"
        return "$name = $rhs,\n"
    }

    private fun writeFile(
        pkg: String,
        fileName: String,
        originating: KSClassDeclaration,
        content: String,
    ) {
        codeGenerator
            .createNewFile(
                Dependencies(false, originating.containingFile!!),
                pkg,
                fileName,
                "kt",
            ).use { out ->
                OutputStreamWriter(out, Charsets.UTF_8).use { it.write(content) }
            }
    }
}

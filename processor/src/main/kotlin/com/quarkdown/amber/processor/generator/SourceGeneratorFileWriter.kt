package com.quarkdown.amber.processor.generator

import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSAnnotated

/**
 * Writes the generated source to a new Kotlin file using the KSP code generator.
 *
 * The created file depends on the containing file of the annotated symbol to ensure proper
 * incremental processing support.
 *
 * @param content Full Kotlin source text to write.
 */
fun <T : KSAnnotated> SourceGenerator<T>.writeFile(content: String) =
    environment.codeGenerator
        .createNewFile(
            dependencies = Dependencies(false, annotated.containingFile!!),
            packageName = packageName,
            fileName = fileName,
            extensionName = "kt",
        ).bufferedWriter()
        .use { it.write(content) }

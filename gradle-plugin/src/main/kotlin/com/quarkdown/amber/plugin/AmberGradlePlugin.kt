package com.quarkdown.amber.plugin

import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.PathSensitivity
import org.gradle.kotlin.dsl.dependencies
import java.io.File

private const val GROUP_ID = "com.quarkdown.amber"
private const val VERSION_RESOURCE_PATH = "/version.txt"

/**
 * KSP option the processor reads the project's resource roots from, needed by `@ExportResource`
 * to look resources up at compile time. Mirrors `RESOURCE_ROOTS_OPTION` of the processor module.
 */
private const val RESOURCE_ROOTS_OPTION = "amber.resourceRoots"

/** Prefix shared by the KSP tasks whose output depends on the project's resources. */
private const val KSP_TASK_PREFIX = "ksp"

class AmberGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.applyKspPlugin()
        target.applyDependencies()
        target.applySourceSetConfiguration()
        target.applyResourceRootsOption()
    }

    /**
     * Applies the KSP plugin to the given project.
     */
    private fun Project.applyKspPlugin() {
        this.pluginManager.apply("com.google.devtools.ksp")
    }

    /**
     * Adds dependencies to the annotations and processor modules if they are present in the root project.
     */
    private fun Project.applyDependencies() {
        val version =
            AmberGradlePlugin::class.java
                .getResource(VERSION_RESOURCE_PATH)
                ?.readText()
                ?.trim()
        require(!version.isNullOrBlank()) { "AmberGradlePlugin: Unable to determine version." }

        dependencies {
            add("implementation", "$GROUP_ID:amber-annotations:$version")
            add("ksp", "$GROUP_ID:amber-processor:$version")
        }
    }

    private fun Project.applySourceSetConfiguration() {
        extensions.findByType(JavaPluginExtension::class.java)?.apply {
            sourceSets.named("main") {
                java.srcDir("build/generated/ksp/main/kotlin")
            }
        }
    }

    /**
     * Exposes the project's resource directories to the processor, which reads resources at
     * compile time on behalf of `@ExportResource`, and makes the KSP tasks depend on their
     * content, so that editing a resource regenerates the sources that embed it.
     */
    private fun Project.applyResourceRootsOption() {
        val sourceSets = extensions.findByType(JavaPluginExtension::class.java)?.sourceSets ?: return

        afterEvaluate {
            val roots = sourceSets.flatMap { it.resources.srcDirs }.distinct()
            if (roots.isEmpty()) return@afterEvaluate

            extensions
                .findByType(KspExtension::class.java)
                ?.arg(RESOURCE_ROOTS_OPTION, roots.joinToString(File.pathSeparator) { it.absolutePath })

            tasks.matching { it.name.startsWith(KSP_TASK_PREFIX) }.configureEach {
                inputs
                    .files(roots)
                    .withPropertyName("amberResources")
                    .withPathSensitivity(PathSensitivity.RELATIVE)
                    .optional()
            }
        }
    }
}

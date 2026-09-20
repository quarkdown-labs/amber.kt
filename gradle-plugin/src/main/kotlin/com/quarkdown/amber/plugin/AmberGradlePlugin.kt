package com.quarkdown.amber.plugin

import com.google.devtools.ksp.gradle.KspExtension
import com.quarkdown.amber.plugin.wiring.AmberWiring
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.PathSensitivity
import org.gradle.kotlin.dsl.dependencies
import java.io.File

private const val GROUP_ID = "com.quarkdown.amber"
private const val VERSION_RESOURCE_PATH = "/version.txt"

private const val KSP_PLUGIN_ID = "com.google.devtools.ksp"
private const val KOTLIN_JVM_PLUGIN_ID = "org.jetbrains.kotlin.jvm"
private const val KOTLIN_MULTIPLATFORM_PLUGIN_ID = "org.jetbrains.kotlin.multiplatform"

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
        // The project is wired the way its own Kotlin plugin, whichever it is, requires.
        target.wireOnPlugin(KOTLIN_MULTIPLATFORM_PLUGIN_ID, AmberWiring.MULTIPLATFORM)
        target.wireOnPlugin(KOTLIN_JVM_PLUGIN_ID, AmberWiring.JVM)
    }

    /**
     * Applies the KSP plugin to the given project.
     */
    private fun Project.applyKspPlugin() {
        this.pluginManager.apply(KSP_PLUGIN_ID)
    }

    /** Wires Amber the [wiring] way, as soon as [pluginId] is applied to this project, if ever. */
    private fun Project.wireOnPlugin(
        pluginId: String,
        wiring: AmberWiring,
    ) {
        pluginManager.withPlugin(pluginId) {
            applyDependencies(wiring)
            wiring.registerGeneratedSources(this@wireOnPlugin)
            applyResourceRootsOption(wiring)
        }
    }

    /**
     * Adds the annotations and processor artifacts to the configurations [wiring] exposes them through.
     */
    private fun Project.applyDependencies(wiring: AmberWiring) {
        val version =
            AmberGradlePlugin::class.java
                .getResource(VERSION_RESOURCE_PATH)
                ?.readText()
                ?.trim()
        require(!version.isNullOrBlank()) { "AmberGradlePlugin: Unable to determine version." }

        dependencies {
            add(wiring.annotationsConfiguration, "$GROUP_ID:amber-annotations:$version")
            add(wiring.processorConfiguration, "$GROUP_ID:amber-processor:$version")
        }
    }

    /**
     * Exposes the project's resource directories to the processor, which reads resources at
     * compile time on behalf of `@ExportResource`, and makes the KSP tasks depend on their
     * content, so that editing a resource regenerates the sources that embed it.
     */
    private fun Project.applyResourceRootsOption(wiring: AmberWiring) {
        // Source sets are inspected after evaluation, as the build script may still reconfigure them.
        afterEvaluate {
            val roots = wiring.resourceRoots(this).distinct()
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

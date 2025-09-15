package com.quarkdown.automerge.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.dependencies

class AutomergeGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.applyKspPlugin()

        target.afterEvaluate {
            target.applyDependencies()
        }

        target.applySourceSetConfiguration()
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
        val (pluginGroup, pluginVersion) = getPluginCoordinates()
        dependencies {
            add("implementation", "$pluginGroup:automerge-annotations:$pluginVersion")
            add("ksp", "$pluginGroup:automerge-processor:$pluginVersion")
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
     * Attempts to determine the plugin's group and version from its package/manifest.
     * Order of resolution:
     *  - Package implementationVersion and implementationVendor
     *  - Manifest attributes Implementation-Version and Implementation-Vendor-Id / Implementation-Vendor
     *  - Fallback: derive group from package name (first two components) and "unspecified" for version
     */
    private fun getPluginCoordinates(): Pair<String, String> {
        val clazz = this::class.java
        val pkg = clazz.`package`

        var version: String? = pkg?.implementationVersion
        var vendorId: String? = pkg?.implementationVendor

        val manifestStream = clazz.classLoader.getResourceAsStream("META-INF/MANIFEST.MF")
        if ((version == null || vendorId == null) && manifestStream != null) {
            try {
                val mf = java.util.jar.Manifest(manifestStream)
                val attrs = mf.mainAttributes
                if (version == null) version = attrs.getValue("Implementation-Version")
                if (vendorId == null) vendorId = attrs.getValue("Implementation-Vendor-Id") ?: attrs.getValue("Implementation-Vendor")
            } catch (_: Exception) {
                // ignore manifest read errors and fall back
            } finally {
                try {
                    manifestStream.close()
                } catch (_: Exception) {
                }
            }
        }

        val group =
            vendorId ?: run {
                val pkgName = pkg?.name ?: "com.quarkdown.automerge"
                val parts = pkgName.split('.')
                if (parts.size >= 2) parts.take(2).joinToString(".") else pkgName
            }

        val ver = version ?: "unspecified"

        return group to ver
    }
}

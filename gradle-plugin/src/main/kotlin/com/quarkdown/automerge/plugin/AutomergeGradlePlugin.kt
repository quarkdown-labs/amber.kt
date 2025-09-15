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
        val group = rootProject.group
        val version = rootProject.version
        dependencies {
            add("implementation", "$group:automerge-annotations:$version")
            add("ksp", "$group:automerge-processor:$version")
        }
    }

    private fun Project.applySourceSetConfiguration() {
        extensions.findByType(JavaPluginExtension::class.java)?.apply {
            sourceSets.named("main") {
                java.srcDir("build/generated/ksp/main/kotlin")
            }
        }
    }
}

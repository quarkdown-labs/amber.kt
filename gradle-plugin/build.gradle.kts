plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

description = "Gradle plugin for `automerge`, the library to merge instances of Kotlin data classes at compile time."

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(gradleApi())
    implementation(localGroovy())
    // Ensure the KSP Gradle plugin is on the classpath so applying it by id works
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.2.10-2.0.2")
}

gradlePlugin {
    plugins {
        create("automerge") {
            id = "com.quarkdown.automerge.gradle-plugin"
            implementationClass = "com.quarkdown.automerge.plugin.AutomergeGradlePlugin"
            displayName = "kotlin-automerge Setup Plugin"
            description = "Applies KSP and wires annotations and processor modules"
        }
    }
}

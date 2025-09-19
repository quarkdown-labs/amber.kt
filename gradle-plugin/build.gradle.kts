plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

description = "Gradle plugin for `amber`, the compile-time utils for Kotlin."

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
        create("amber") {
            id = "com.quarkdown.amber"
            implementationClass = "com.quarkdown.amber.plugin.AmberGradlePlugin"
            displayName = "Amber Setup Plugin"
            description = "Applies KSP and wires annotations and processor modules"
        }
    }
}

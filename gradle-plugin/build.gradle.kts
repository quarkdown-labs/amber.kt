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
    implementation("com.google.devtools.ksp:com.google.devtools.ksp.gradle.plugin:2.3.4")
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

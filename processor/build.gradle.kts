plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp") version "2.2.10-2.0.2"
    `maven-publish`
}

description = "Compile-time processor for `automerge`, the library to merge instances of Kotlin data classes at compile time."

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.dagger:dagger-compiler:2.51.1")
    ksp("com.google.dagger:dagger-compiler:2.51.1")
    implementation(project(":annotations"))
}

plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp") version "2.2.10-2.0.2"
}

group = "com.quarkdown.amber"
version = parent?.version ?: "0.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":annotations"))
    ksp(project(":processor"))
    testImplementation(kotlin("test"))
}

sourceSets.main {
    java.srcDirs("build/generated/ksp/main/kotlin")
}

// Supplies @ExportResource with the resource roots to read from. Consumer projects get this from
// the `com.quarkdown.amber` Gradle plugin, which this module does not apply, as it depends on the
// local processor rather than on a published one.
ksp {
    arg(
        "amber.resourceRoots",
        sourceSets.flatMap { it.resources.srcDirs }.joinToString(File.pathSeparator) { it.absolutePath },
    )
}

tasks.test {
    useJUnitPlatform()
}

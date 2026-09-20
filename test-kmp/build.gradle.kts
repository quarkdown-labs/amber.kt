import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp") version "2.2.10-2.0.2"
}

group = "com.quarkdown.amber"
version = parent?.version ?: "0.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvm()
    js(IR) {
        nodejs()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        nodejs()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmWasi {
        nodejs()
    }

    // The host's own native target, so that every platform family is covered wherever the build runs.
    when (System.getProperty("os.name")) {
        "Mac OS X" -> macosArm64()
        "Linux" -> linuxX64()
        else -> mingwX64()
    }

    sourceSets {
        commonMain {
            // Where the processor generates common sources, as the Amber Gradle plugin declares them.
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
            dependencies {
                implementation(project(":annotations"))
            }
        }
        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

dependencies {
    // Common sources are processed once, into common generated sources every target then compiles.
    add("kspCommonMainMetadata", project(":processor"))
}

ksp {
    arg(
        "amber.resourceRoots",
        kotlin.sourceSets
            .flatMap { it.resources.srcDirs }
            .joinToString(File.pathSeparator) { it.absolutePath },
    )
}

// Every compilation consumes the common generated sources, hence must wait for them.
tasks.withType<KotlinCompilationTask<*>>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

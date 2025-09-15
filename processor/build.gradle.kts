plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp") version "2.2.10-2.0.2"
    `maven-publish`
}

group = "com.quarkdown.automerge"
version = parent?.version ?: "0.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.dagger:dagger-compiler:2.51.1")
    ksp("com.google.dagger:dagger-compiler:2.51.1")
    implementation(project(":annotations"))
}

mavenPublishing {
    coordinates(project.group.toString(), "processor", project.version.toString())

    pom {
        name.set("automerge-processor")
        description.set("Compile-time processor for `automerge`, the library to merge instances of Kotlin data classes at compile time.")
        url.set("https://github.com/quarkdown-labs/kotlin-automerge/")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("iamgio")
                name.set("Giorgio Garofalo")
                url.set("https://github.com/iamgio")
            }
        }
        scm {
            url.set("https://github.com/quarkdown-labs/kotlin-automerge/")
            connection.set("scm:git:git://github.com/quarkdown-labs/kotlin-automerge.git")
            developerConnection.set("scm:git:ssh://git@github.com/quarkdown-labs/kotlin-automerge.git")
        }
    }
}

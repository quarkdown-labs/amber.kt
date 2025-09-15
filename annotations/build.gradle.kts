plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "com.quarkdown.automerge"
version = parent?.version ?: "0.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
}

mavenPublishing {
    coordinates(project.group.toString(), "annotations", project.version.toString())

    pom {
        name.set("automerge-annotations")
        description.set("Runtime annotations for `automerge`, the library to merge instances of Kotlin data classes at compile time.")
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

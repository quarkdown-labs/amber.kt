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

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = project.group.toString()
            artifactId = "annotations"
            version = project.version.toString()
            pom {
                name.set("automerge-annotations")
                description.set("Annotations for AutoMerge KSP processor to generate merge methods for Kotlin data classes.")
                url.set("https://github.com/quarkdown-labs/kotlin-automerge")
                licenses {
                    license {
                        name.set("The MIT License")
                        url.set("https://opensource.org/license/mit")
                    }
                }
                developers {
                    developer {
                        id.set("quarkdown")
                        name.set("Quarkdown")
                    }
                }
                scm {
                    url.set("https://github.com/quarkdown-labs/kotlin-automerge")
                    connection.set("scm:git:git://github.com/quarkdown-labs/kotlin-automerge.git")
                    developerConnection.set("scm:git:ssh://git@github.com/quarkdown-labs/kotlin-automerge.git")
                }
            }
        }
    }
}

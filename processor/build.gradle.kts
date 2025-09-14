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

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = project.group.toString()
            artifactId = "processor"
            version = project.version.toString()
            pom {
                name.set("kotlin-automerge-processor")
                description.set("KSP processor that generates merge methods for Kotlin data classes annotated with @AutoMerge.")
                url.set("https://github.com/quarkdown-labs/kotlin-automerge")
                licenses {
                    license {
                        name.set("Apache 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
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

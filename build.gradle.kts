plugins {
    kotlin("jvm") version "2.2.10"
    id("com.vanniktech.maven.publish") version "0.34.0"
}

group = "com.quarkdown.automerge"
version = file("version.txt").readText().trim()

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

val publishingSubprojects =
    listOf(
        "annotations",
        "processor",
        "gradle-plugin",
    )

subprojects.filter { it.name in publishingSubprojects }.forEach { project ->
    val projectName = "automerge-" + project.name

    project.apply(plugin = "com.vanniktech.maven.publish")

    project.mavenPublishing {
        coordinates(group.toString(), projectName, version.toString())

        publishToMavenCentral()
        signAllPublications()

        pom {
            name.set(projectName)
            description.set(project.description)
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
}

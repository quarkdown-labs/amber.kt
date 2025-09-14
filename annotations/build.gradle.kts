plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "com.quarkdown.automerge"
version = "0.1.0"

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
        }
    }
}

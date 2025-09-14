plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp") version "2.2.10-2.0.2"
    `maven-publish`
}

group = "com.quarkdown.automerge"
version = "0.1.0"

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
        }
    }
}

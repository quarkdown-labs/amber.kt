plugins {
    kotlin("jvm") version "2.2.10"
    id("com.vanniktech.maven.publish") version "0.34.0"
    signing
}

group = "com.quarkdown.automerge"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

subprojects {
    apply(plugin = "com.vanniktech.maven.publish")
}

plugins {
    kotlin("jvm")
    id("com.google.devtools.ksp") version "2.2.10-2.0.2"
}

group = "com.quarkdown.automerge"
version = "0.1.0"

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

tasks.test {
    useJUnitPlatform()
}

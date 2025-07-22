plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()

    // Required for InteraKTionsUnleashed
    maven("https://repo.perfectdreams.net/")
    maven("https://jitpack.io")
}

dependencies {
    implementation(project(":"))
}

kotlin {
    jvmToolchain(21)
}
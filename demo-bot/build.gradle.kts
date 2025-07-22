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
    implementation("me.hechfx:interaktions-unleashed:1.0.1")
}

kotlin {
    jvmToolchain(21)
}
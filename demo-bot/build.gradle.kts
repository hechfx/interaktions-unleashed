plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()

    // Required for InteraKTionsUnleashed
    maven("https://repo.perfectdreams.net/")
}

dependencies {
    implementation(libs.jda)
    implementation(libs.jda.ktx)

    implementation(libs.harmony.logging)
    implementation(libs.logback.classic)

    implementation("me.hechfx:interaktions-unleashed:1.0.0")
}

kotlin {
    jvmToolchain(21)
}
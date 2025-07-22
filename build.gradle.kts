plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
    `maven-publish`
}

repositories {
    mavenCentral()
    maven("https://repo.perfectdreams.net/")
    maven("https://jitpack.io")
}

dependencies {
    api(libs.jda)
    api(libs.jda.ktx)

    api(libs.harmony.logging)
    api(libs.logback.classic)

    implementation(libs.caffeine)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.google.guava)
}

kotlin {
    jvmToolchain(21)
}

publishing {
    publications {
        register("PerfectDreams", MavenPublication::class.java) {
            from(components["java"])

            groupId = "me.hechfx"
            artifactId = "interaktions-unleashed"
            version = "1.0.1"
        }
    }

    repositories {
        maven {
            name = "PerfectDreams"
            url = uri("https://repo.perfectdreams.net/")
            credentials {
                username = System.getenv("PERFECTDREAMS_USERNAME") ?: ""
                password = System.getenv("PERFECTDREAMS_PASSWORD") ?: ""
            }
        }
    }
}
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.serialization)
    `maven-publish`
}

repositories {
    mavenCentral()
    maven("https://m2.dv8tion.net/releases")
    maven("https://repo.perfectdreams.net/")
}

dependencies {
    implementation(libs.jda)
    implementation(libs.jda.ktx)

    implementation(libs.caffeine)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.harmony.logging)
    implementation(libs.logback.classic)

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
            version = "1.0.0"
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
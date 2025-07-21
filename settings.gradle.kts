rootProject.name = "interaktions-unleashed"

pluginManagement {
    repositories {
        // I don't know why but if "gradlePluginPortal()" is before our custom Maven repo, the i18nHelper plugin isn't found
        maven("https://repo.perfectdreams.net/")
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}

include("demo-bot")
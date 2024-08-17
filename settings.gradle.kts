@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        includeBuild("plugins")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "AutoCrop"
include (":app")
include (":core:domain")
include (":core:datastore")
include (":core:cropbundle")
include (":core:screenshotlistening")
include (":core:common")
include (":core:flowfield")
include (":core:opencv")
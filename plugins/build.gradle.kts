plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
    }
}

dependencies {
    implementation(libs.android.gradlePlugin)
    implementation(libs.kotlin.gradlePlugin)
    implementation(libs.ksp.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("library") {
            id = "autocrop.library"
            implementationClass = "LibraryPlugin"
        }

        register("hilt") {
            id = "autocrop.hilt"
            implementationClass = "HiltPlugin"
        }
    }
}

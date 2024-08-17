plugins {
    alias(libs.plugins.autocrop.library)
}

dependencies {
    api(files("libs/processing-core-4.3.0.jar"))
    implementation (libs.google.guava)
}
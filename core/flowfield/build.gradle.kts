plugins {
    alias(libs.plugins.autocrop.library)
}

dependencies {
    api(files("libs/processing-core-4.6.1.jar"))
    implementation(libs.google.guava)
}

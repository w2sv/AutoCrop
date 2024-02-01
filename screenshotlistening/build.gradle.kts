plugins {
    alias(libs.plugins.autocrop.library)
    alias(libs.plugins.autocrop.hilt)
}

dependencies {
    implementation(projects.cropbundle)
    implementation(projects.common)

    implementation (libs.androidx.core)
    implementation (libs.slimber)
    implementation (libs.androidutils)
    implementation (libs.kotlinutils)
    implementation (libs.google.guava)
    implementation (libs.androidx.localbroadcastmanager)

    testImplementation (libs.bundles.unitTest)
}
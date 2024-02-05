plugins {
    alias(libs.plugins.autocrop.library)
    alias(libs.plugins.autocrop.hilt)
}

dependencies {
    implementation(projects.cropbundle)
    implementation(projects.domain)
    implementation(projects.common)
    implementation(projects.datastore)

    implementation(libs.androidx.core)
    implementation(libs.slimber)
    implementation(libs.androidutils)
    implementation(libs.kotlinutils)
    implementation(libs.google.guava)

    testImplementation(libs.bundles.unitTest)
}
plugins {
    alias(libs.plugins.autocrop.library)
    alias(libs.plugins.autocrop.hilt)
}

dependencies {
    implementation(projects.core.cropbundle)
    implementation(projects.core.domain)
    implementation(projects.core.common)
    implementation(projects.core.datastore)

    implementation(libs.androidx.core)
    implementation(libs.slimber)
    implementation(libs.androidutils.core)
    implementation(libs.kotlinutils)
    implementation(libs.kotlindelegates)
    implementation(libs.google.guava)

    testImplementation(libs.bundles.unitTest)
}
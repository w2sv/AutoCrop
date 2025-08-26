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
    implementation(libs.w2sv.androidutils.core)
    implementation(libs.w2sv.kotlinutils)
    implementation(libs.w2sv.kotlindelegates)
    implementation(libs.google.guava)

    testImplementation(libs.bundles.unitTest)
}

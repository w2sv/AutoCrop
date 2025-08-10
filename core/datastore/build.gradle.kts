plugins {
    alias(libs.plugins.autocrop.library)
    alias(libs.plugins.autocrop.hilt)
}

dependencies {
    implementation(projects.core.domain)

    implementation(libs.androidx.datastore.preferences)
    api(libs.w2sv.datastoreutils.preferences)
    implementation(libs.slimber)

    implementation(libs.androidutils.core)
    implementation(libs.w2sv.kotlinutils)
}

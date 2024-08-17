plugins {
    alias(libs.plugins.autocrop.library)
    alias(libs.plugins.autocrop.hilt)
}

dependencies {
    implementation(projects.core.domain)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.datastoreutils.preferences)
    implementation(libs.slimber)

    implementation(libs.androidutils.core)
    implementation(libs.kotlinutils)
}
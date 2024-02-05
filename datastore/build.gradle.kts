plugins {
    alias(libs.plugins.autocrop.library)
    alias(libs.plugins.autocrop.hilt)
}

dependencies {
    implementation(projects.domain)

    implementation (libs.androidx.datastore.preferences)
    implementation (libs.slimber)

    implementation (libs.androidutils)
}
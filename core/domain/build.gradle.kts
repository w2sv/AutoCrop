plugins {
    alias(libs.plugins.autocrop.library)
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.slimber)

    implementation(libs.datastoreutils.preferences)
    implementation(libs.kotlinutils)
    implementation(libs.androidutils.core)
}
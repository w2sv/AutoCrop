plugins {
    alias(libs.plugins.autocrop.library)
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.slimber)

    implementation(libs.w2sv.datastoreutils.preferences)
    implementation(libs.w2sv.kotlinutils)
    implementation(libs.androidutils.core)
}

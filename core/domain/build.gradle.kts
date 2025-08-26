plugins {
    alias(libs.plugins.autocrop.library)
    alias(libs.plugins.kotlin.parcelize)
}

dependencies {
    implementation(projects.core.common)
    implementation(libs.androidx.core)
    implementation(libs.slimber)
    implementation(libs.w2sv.datastoreutils.preferences)
    implementation(libs.w2sv.kotlinutils)
    implementation(libs.androidutils.core)
}

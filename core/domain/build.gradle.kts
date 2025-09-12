plugins {
    alias(libs.plugins.autocrop.library)
    alias(libs.plugins.kotlin.parcelize)
}

dependencies {
    implementation(projects.core.common)
    implementation(libs.androidx.core)
    implementation(libs.slimber)
    api(libs.w2sv.datastoreutils.preferences)
    api(libs.w2sv.datastoreutils.datastoreflow)
    implementation(libs.w2sv.kotlinutils)
    implementation(libs.w2sv.androidutils.core)
}

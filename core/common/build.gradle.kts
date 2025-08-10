plugins {
    alias(libs.plugins.autocrop.library)
    alias(libs.plugins.autocrop.hilt)
}

android {
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.google.material)
    implementation(libs.snacky)
    implementation(libs.slimber)

    implementation(libs.w2sv.kotlinutils)
    api(libs.w2sv.permissionhandler)
    implementation(libs.androidutils.core)
    implementation(libs.w2sv.androidutils.lifecycle)
}

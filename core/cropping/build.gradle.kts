plugins {
    alias(libs.plugins.autocrop.library)
    alias(libs.plugins.autocrop.hilt)
    alias(libs.plugins.kotlin.parcelize)
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.common)
    implementation(libs.androidx.core)
    implementation(libs.w2sv.kotlinutils)
    implementation(libs.w2sv.androidutils.core)
    implementation(libs.slimber)
    implementation(libs.opencv)

    //    // ---------------
    //    // unitTest
    //    testImplementation libs.bundles.unitTest
    //
    //    // ---------------
    //    // androidTest
    //    androidTestImplementation libs.bundles.androidTest
    //    androidTestRuntimeOnly libs.junit5.mannodermaus.runner
    //    androidTestImplementation 'androidx.test:monitor:1.6.1'
}

plugins {
    alias(libs.plugins.autocrop.library)
    alias(libs.plugins.autocrop.hilt)
    id("kotlin-parcelize")
}

dependencies {
    implementation(projects.opencv)
    implementation(projects.domain)

    implementation (libs.androidx.core)
    implementation (libs.kotlinutils)
    implementation (libs.slimber)
    implementation (libs.androidutils)

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
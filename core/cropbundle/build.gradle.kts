plugins {
    alias(libs.plugins.autocrop.library)
    alias(libs.plugins.autocrop.hilt)
    alias(libs.plugins.kotlin.parcelize) 
}

dependencies {
    implementation(projects.core.opencv)
    implementation(projects.core.domain)

    implementation (libs.androidx.core)
    implementation (libs.kotlinutils)
    implementation (libs.slimber)
    implementation (libs.androidutils.core)

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
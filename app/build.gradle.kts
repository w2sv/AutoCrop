import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.application)
    alias(libs.plugins.play)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.autocrop.hilt)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.androidx.navigation.safe.args)
}

kotlin {
    jvmToolchain(libs.versions.java.get().toInt())
}

android {
    val packageName = "com.w2sv.autocrop"

    namespace = packageName
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = packageName
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.compileSdk.get().toInt()

        versionCode = project.findProperty("versionCode")!!.toString().toInt()
        versionName = version.toString()

        @Suppress("UnstableApiUsage")
        externalNativeBuild {
            cmake {
                cppFlags("-frtti -fexceptions")
                abiFilters("x86", "x86_64", "armeabi-v7a", "arm64-v8a")
                arguments("-DOpenCV_DIR=${rootProject.projectDir}/opencv/native")
            }
        }

        // Name built bundles "{versionName}-{buildFlavor}.aab"
        setProperty("archivesBaseName", versionName)

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        //        testInstrumentationRunnerArguments runnerBuilder: 'de.mannodermaus.junit5.AndroidJUnit5Builder'
    }

    signingConfigs {
        create("release") {
            rootProject.file("keystore.properties").let { file ->
                if (file.exists()) {
                    val keystoreProperties = Properties()
                    keystoreProperties.load(FileInputStream(file))

                    storeFile = rootProject.file("keys.jks")
                    storePassword = keystoreProperties["storePassword"] as String
                    keyAlias = keystoreProperties["keyAlias"] as String
                    keyPassword = keystoreProperties["keyPassword"] as String
                }
            }
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".debug"
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    packaging {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }

    hilt {
        enableAggregatingTask = true  // Fixes warning
    }

    @Suppress("UnstableApiUsage")
    testOptions {
        animationsDisabled = true
    }

    // Name built apks "{versionName}.apk"
    applicationVariants.all {
        outputs
            .forEach { output ->
                (output as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                    "${versionName}.apk"
            }
    }
}

// https://github.com/Triple-T/gradle-play-publisher
play {
    serviceAccountCredentials.set(file("service-account-key.json"))
    defaultToAppBundles.set(true)
    artifactDir.set(file("build/outputs/bundle/release"))
}

dependencies {
    // Project Modules
    implementation(projects.core.cropbundle)
    implementation(projects.core.domain)
    implementation(projects.core.datastore)
//    implementation(projects.core.screenshotlistening)
    implementation(projects.core.common)
    implementation(projects.core.flowfield)
    implementation(projects.core.opencv)

    // Androidx
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core)
    implementation(libs.androidx.viewmodel)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.splashscreen)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.androidx.navigation.fragment)

    // Owned
    implementation(libs.kotlinutils)
    implementation(libs.viewboundcontroller)
    implementation(libs.androidutils.core)
    implementation(libs.androidutils.lifecycle)
    implementation(libs.androidutils.view)
    implementation(libs.viewanimations)
    implementation(libs.bidirectionalviewpager)

    // Other
    implementation(libs.slimber)
    implementation(libs.animatoo)
    implementation(libs.lottie)
    implementation(libs.simplestorage)
    implementation(libs.google.material)

    //    // ---------------
    //    // unitTest
    //    testImplementation(libs.bundles.unitTest)
    //
    //    // ---------------
    //    // androidTest
    //    androidTestImplementation(libs.bundles.androidTest)
    //    androidTestRuntimeOnly(libs.junit5.mannodermaus.runner)
    //
    //    // espresso
    //    androidTestImplementation(libs.androidx.espresso.core)
    //    androidTestImplementation(libs.androidx.espresso.intents)
}
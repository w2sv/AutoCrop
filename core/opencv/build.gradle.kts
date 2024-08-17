plugins {
    alias(libs.plugins.autocrop.library)
}

android {
    namespace = "org.opencv"

    defaultConfig {
        @Suppress("UnstableApiUsage")
        externalNativeBuild {
            cmake {
                arguments("-DANDROID_STL=c++_shared")
                targets("opencv_jni_shared")
            }
        }
    }

    buildTypes {
        debug {
            packaging {
                jniLibs {
                    keepDebugSymbols.add("**/*.so")  // controlled by OpenCV CMake scripts
                }
            }
        }
        release {
            packaging {
                jniLibs {
                    keepDebugSymbols.add("**/*.so")  // controlled by OpenCV CMake scripts
                }
            }
        }
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs(listOf("native/libs"))
            java.srcDirs(listOf("java/src"))
            aidl.srcDirs(listOf("java/src"))
            res.srcDirs(listOf("java/res"))
        }
    }

    externalNativeBuild {
        cmake {
            path("${project.projectDir}/libcxx_helper/CMakeLists.txt")
        }
    }
}
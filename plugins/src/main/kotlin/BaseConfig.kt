import com.android.build.gradle.BaseExtension
import org.gradle.api.Project
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

internal fun Project.baseConfig() {
    with(extensions) {
        configure<KotlinProjectExtension> {
            jvmToolchain {
                languageVersion.set(JavaLanguageVersion.of(libs.findVersionInt("java")))
            }
        }
        configure<BaseExtension> {
            // Set namespace to com.w2sv.<module-name>
            namespace = "com.w2sv." + path.removePrefix(":").replace(':', '.')

            defaultConfig {
                minSdk = libs.findVersionInt("minSdk")
                targetSdk = libs.findVersionInt("compileSdk")
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                // testInstrumentationRunnerArguments runnerBuilder: 'de.mannodermaus.junit5.AndroidJUnit5Builder'
            }
            compileSdkVersion(libs.findVersionInt("compileSdk"))
            testOptions {
                animationsDisabled = true

                unitTests {
                    isReturnDefaultValues = true
                    isIncludeAndroidResources = true
                }
            }
        }
    }
}
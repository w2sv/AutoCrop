import org.gradle.api.Plugin
import org.gradle.api.Project
import shared.Namespace
import shared.applyBaseConfig
import shared.applyPlugins
import shared.catalog

@Suppress("UNUSED")
class ApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.applyPlugins("android-application", "kotlin-android", catalog = catalog)
            applyBaseConfig(Namespace.Manual("com.w2sv.autocrop"))
        }
    }
}

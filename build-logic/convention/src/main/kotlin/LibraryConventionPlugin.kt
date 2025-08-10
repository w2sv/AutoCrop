import org.gradle.api.Plugin
import org.gradle.api.Project
import shared.applyBaseConfig
import shared.applyPlugins
import shared.catalog

@Suppress("UNUSED")
class LibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.applyPlugins("android-library", "kotlin-android", catalog = catalog)
            applyBaseConfig()
        }
    }
}

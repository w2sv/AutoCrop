import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import shared.applyPlugins
import shared.catalog
import shared.library

@Suppress("UNUSED")
class HiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.applyPlugins("ksp", "hilt", catalog = catalog)

            dependencies {
                "implementation"(library("google.hilt"))
                "ksp"(library("google.hilt.compiler"))
            }
        }
    }
}

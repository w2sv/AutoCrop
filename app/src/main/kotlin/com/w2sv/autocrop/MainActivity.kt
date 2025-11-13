package com.w2sv.autocrop

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.w2sv.autocrop.databinding.ActivityMainBinding
import com.w2sv.autocrop.ui.util.resolution
import com.w2sv.autocrop.ui.util.view.viewBinding
import com.w2sv.flowfield.PerlinNoiseFlowFieldSketch
import com.w2sv.kotlinutils.coroutines.flow.collectOn
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import processing.android.PFragment
import processing.core.PApplet
import slimber.log.i

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val binding by viewBinding(ActivityMainBinding::inflate)

    private var pFragment: PFragment? = null
    private var sketchPaused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        inflateFlowField()
        val navController = findNavController(R.id.nav_host_fragment)
            .apply {
                addOnDestinationChangedListener { _, destination, _ ->
                    when (destination.id) {
                        R.id.crop_inspection_screen -> {
                            pFragment?.sketch?.noLoop()
                            sketchPaused = true
                        }

                        R.id.home_screen if (sketchPaused) -> {
                            inflateFlowField()
                            sketchPaused = false
                        }
                    }
                }
            }

        if (BuildConfig.DEBUG) {
            navController.apply {
                if (savedInstanceState == null && BuildConfig.START_WITH_CROP_SCREEN) {
                    inflateGraphWithStartDestination(R.id.crop_nav_graph)
                }
                setupBackStackLogging(lifecycleScope)
            }
        }
    }

    private fun flowFieldSketch(): PApplet {
        val resolution = windowManager.resolution
        return PerlinNoiseFlowFieldSketch(resolution.x, resolution.y)
    }

    private fun inflateFlowField() {
        pFragment = PFragment(flowFieldSketch())
        supportFragmentManager
            .beginTransaction()
            .replace(
                binding.flowFieldCanvas.id,
                requireNotNull(pFragment)
            )
            .commitAllowingStateLoss() // Fixes java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
    }
}

private fun FragmentActivity.findNavController(@IdRes id: Int): NavController =
    (supportFragmentManager.findFragmentById(id) as NavHostFragment).navController

@SuppressLint("RestrictedApi")
private fun NavController.setupBackStackLogging(scope: CoroutineScope) {
    currentBackStack.collectOn(scope) { backStackEntries ->
        i { "BackStack: ${backStackEntries.map { it.destination.displayName.substringAfterLast("/") }}" }
    }
}

private fun NavController.inflateGraphWithStartDestination(destinationId: Int) {
    graph = navInflater.inflate(R.navigation.nav_graph).apply {
        setStartDestination(destinationId)
    }
}

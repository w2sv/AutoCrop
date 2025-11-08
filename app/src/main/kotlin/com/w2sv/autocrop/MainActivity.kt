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
import slimber.log.i

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val binding by viewBinding(ActivityMainBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        inflateFlowField()

        if (BuildConfig.DEBUG) {
            findNavController(R.id.nav_host_fragment).apply {
                if (savedInstanceState == null && BuildConfig.START_WITH_CROP_SCREEN) {
                    inflateGraphWithStartDestination(R.id.crop_nav_graph)
                }
                setupBackStackLogging(lifecycleScope)
            }
        }
    }

    private fun inflateFlowField() {
        val resolution = windowManager.resolution
        supportFragmentManager
            .beginTransaction()
            .add(
                binding.flowFieldCanvas.id,
                PFragment(PerlinNoiseFlowFieldSketch(resolution.x, resolution.y))
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

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
import com.w2sv.autocrop.ui.util.view.viewBinding
import com.w2sv.kotlinutils.coroutines.flow.collectOn
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import slimber.log.i

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding by viewBinding(ActivityMainBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val navController = findNavController(R.id.nav_host_fragment)
            .apply {
                addOnDestinationChangedListener { _, destination, _ ->
                    when (destination.id) {
                        R.id.crop_inspection_screen -> binding.flowFieldView.onPause()
                        R.id.home_screen -> binding.flowFieldView.onResume()
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

    override fun onPause() {
        super.onPause()
        binding.flowFieldView.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.flowFieldView.onResume()
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

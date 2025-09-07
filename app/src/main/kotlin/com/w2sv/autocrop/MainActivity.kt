package com.w2sv.autocrop

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.w2sv.kotlinutils.coroutines.flow.collectOn
import dagger.hilt.android.AndroidEntryPoint
import slimber.log.i

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        if (BuildConfig.DEBUG) {
            findNavController(R.id.nav_host_fragment).currentBackStack.collectOn(lifecycleScope) { backStackEntries ->
                i { "BackStack: ${backStackEntries.map { it.destination.displayName }}" }
            }
        }
    }
}

private fun FragmentActivity.findNavController(@IdRes id: Int): NavController =
    (supportFragmentManager.findFragmentById(id) as NavHostFragment).navController

package com.w2sv.autocrop.ui.util

import android.os.Bundle
import androidx.annotation.IdRes
import androidx.annotation.MainThread
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavGraph
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.Navigator
import androidx.navigation.navOptions
import com.w2sv.autocrop.R

@MainThread
fun NavController.navigateAnimated(
    directions: NavDirections,
    extras: Navigator.Extras? = null,
    optionsBuilder: NavOptionsBuilder.() -> Unit = {}
) {
    navigateAnimated(
        resId = directions.actionId,
        args = directions.arguments,
        extras = extras,
        optionsBuilder = optionsBuilder
    )
}

@MainThread
fun NavController.navigateAnimatedAndPopCurrentDestination(
    directions: NavDirections,
    extras: Navigator.Extras? = null,
    optionsBuilder: NavOptionsBuilder.() -> Unit = {}
) {
    navigateAnimated(
        resId = directions.actionId,
        args = directions.arguments,
        extras = extras,
        optionsBuilder = {
            popUpTo(checkNotNull(currentDestination).id, { inclusive = true })
            optionsBuilder()
        }
    )
}

@MainThread
fun NavController.navigateAnimated(
    @IdRes resId: Int,
    args: Bundle? = null,
    extras: Navigator.Extras? = null,
    optionsBuilder: NavOptionsBuilder.() -> Unit = {}
) {
    navigate(
        resId = resId,
        args = args,
        navigatorExtras = extras,
        navOptions = navOptions {
            launchSingleTop = true
            anim {
                enter = R.anim.slide_in_right
                exit = R.anim.slide_out_left
                popEnter = R.anim.slide_in_left
                popExit = R.anim.slide_out_right
            }
            optionsBuilder()
        }
    )
}

/**
 * Returns the current subgraph (the parent graph of the active destination),
 * or null if no active destination exists.
 */
fun NavController.currentSubGraph(): NavGraph? =
    currentDestination?.parent

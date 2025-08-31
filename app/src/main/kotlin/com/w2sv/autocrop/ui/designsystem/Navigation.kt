package com.w2sv.autocrop.ui.designsystem

import androidx.annotation.MainThread
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.Navigator
import androidx.navigation.navOptions
import com.w2sv.autocrop.R

@MainThread
fun NavController.navigateAnimated(directions: NavDirections, extras: Navigator.Extras? = null) {
    navigate(
        resId = directions.actionId,
        args = directions.arguments,
        navigatorExtras = extras,
        navOptions = navOptions {
            anim {
                enter = R.anim.slide_in_right
                exit = R.anim.slide_out_left
                popEnter = R.anim.slide_in_left
                popExit = R.anim.slide_out_right
            }
        }
    )
}

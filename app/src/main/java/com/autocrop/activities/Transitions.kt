package com.autocrop.activities

import android.app.Activity
import com.blogspot.atifsoftwares.animatoolib.Animatoo


fun Activity.proceedTransitionAnimation() {
    Animatoo.animateSwipeLeft(this)
}

fun Activity.returnTransitionAnimation() {
    Animatoo.animateSwipeRight(this)
}

fun Activity.restartTransitionAnimation() {
    Animatoo.animateFade(this)
}
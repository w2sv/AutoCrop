package com.autocrop.activities

import com.blogspot.atifsoftwares.animatoolib.Animatoo

object ActivityTransitions{
    val PROCEED = Animatoo::animateSwipeLeft
    val RETURN = Animatoo::animateSwipeRight
    val RESTART = Animatoo::animateFade
}

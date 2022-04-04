package com.autocrop.uicontroller.fragment

import android.app.Activity
import androidx.fragment.app.Fragment

abstract class ExtendedFragment<A: Activity>: Fragment(){
    /**
     * Retyped [androidx.fragment.app.Fragment.requireActivity]
     */
    @Suppress("UNCHECKED_CAST")
    val typedActivity: A by lazy {
        requireActivity() as A
    }
}
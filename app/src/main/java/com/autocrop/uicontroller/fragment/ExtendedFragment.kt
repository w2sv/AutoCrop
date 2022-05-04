package com.autocrop.uicontroller.fragment

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.autocrop.utils.BlankFun

abstract class ExtendedFragment<A: Activity>: Fragment(){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = withPostponedEnterTransition {
        super.onViewCreated(view, savedInstanceState)
        onViewCreatedCore(savedInstanceState)
    }

    private inline fun withPostponedEnterTransition(f: BlankFun){
        postponeEnterTransition()
        f()
        startPostponedEnterTransition()
    }

    open fun onViewCreatedCore(savedInstanceState: Bundle?){}

    /**
     * Retyped [androidx.fragment.app.Fragment.requireActivity]
     */
    @Suppress("UNCHECKED_CAST")
    val castedActivity: A by lazy {
        requireActivity() as A
    }
}
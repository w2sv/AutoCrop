package com.w2sv.autocrop.activities

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.viewbinding.ViewBinding
import com.w2sv.viewboundcontroller.ViewBoundFragment

abstract class ApplicationFragment<VB : ViewBinding>(bindingClass: Class<VB>) :
    ViewBoundFragment<VB>(bindingClass) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        postponeEnterTransition()

        super.onViewCreated(view, savedInstanceState)

        (view.parent as? ViewGroup)?.doOnPreDraw {
            startPostponedEnterTransition()
        }
    }

    @Suppress("UNCHECKED_CAST")
    protected fun <A : Activity> castActivity(): A =
        requireActivity() as A

    protected fun getFragmentHostingActivity(): FragmentHostingActivity =
        castActivity()
}
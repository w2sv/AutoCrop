package com.w2sv.autocrop.controller

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.viewbinding.ViewBinding
import com.w2sv.autocrop.controller.activity.FragmentHostingActivity
import com.w2sv.autocrop.controller.activity.retriever.FragmentHostingActivityRetriever
import com.w2sv.viewboundcontroller.ViewBoundFragment

abstract class ApplicationFragment<VB : ViewBinding>(bindingClass: Class<VB>) :
    ViewBoundFragment<VB>(bindingClass),
    FragmentHostingActivityRetriever {

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

    override val fragmentHostingActivity: FragmentHostingActivity
        get() = requireActivity() as FragmentHostingActivity
}
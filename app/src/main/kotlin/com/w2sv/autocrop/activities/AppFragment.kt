package com.w2sv.autocrop.activities

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.viewbinding.ViewBinding
import com.w2sv.viewboundcontroller.ViewBoundFragment

abstract class AppFragment<VB : ViewBinding>(bindingClass: Class<VB>) :
    ViewBoundFragment<VB>(bindingClass) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        postponeEnterTransition()

        super.onViewCreated(view, savedInstanceState)

        (view.parent as? ViewGroup)?.doOnPreDraw {
            startPostponedEnterTransition()
        }
    }
}
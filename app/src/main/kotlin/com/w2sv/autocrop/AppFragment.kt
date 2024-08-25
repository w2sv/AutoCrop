package com.w2sv.autocrop

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.viewbinding.ViewBinding
import com.w2sv.autocrop.util.navController
import com.w2sv.autocrop.util.registerOnBackPressedHandler
import com.w2sv.viewboundcontroller.ViewBoundFragment

abstract class AppFragment<VB : ViewBinding>(bindingClass: Class<VB>) :
    ViewBoundFragment<VB>(bindingClass) {

    protected val navController by navController()
    open val onBackPressed: (() -> Unit)? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        onBackPressed?.let { registerOnBackPressedHandler(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        postponeEnterTransition()

        (view.parent as? ViewGroup)?.doOnPreDraw {
            startPostponedEnterTransition()
        }
    }
}
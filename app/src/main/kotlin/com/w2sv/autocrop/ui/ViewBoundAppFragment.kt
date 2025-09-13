package com.w2sv.autocrop.ui

import android.content.Context
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.w2sv.autocrop.ui.util.registerOnBackPressedHandler
import com.w2sv.kotlinutils.threadUnsafeLazy
import com.w2sv.viewboundcontroller.ViewBoundFragment

abstract class ViewBoundAppFragment<VB : ViewBinding>(bindingClass: Class<VB>) : ViewBoundFragment<VB>(bindingClass) {

    protected val navController by threadUnsafeLazy { findNavController() }
    open val onBackPressed: (() -> Unit)? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        onBackPressed?.let { registerOnBackPressedHandler(it) }
    }
}

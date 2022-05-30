package com.autocrop.uicontroller.fragment

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.autocrop.uicontroller.ViewModelRetriever
import com.autocrop.utilsandroid.TypedActivityRetriever

abstract class ApplicationFragment<A: Activity, VB: ViewBinding, VM: ViewModel>(
    viewModelClass: Class<VM>,
    bindingClass: Class<VB>):
        ViewBoundFragment<VB>(bindingClass),
        ViewModelRetriever<VM>,
        TypedActivityRetriever<A>{

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        postponeEnterTransition()

        super.onViewCreated(view, savedInstanceState)
        onViewCreatedCore(savedInstanceState)

        (view.parent as? ViewGroup)?.doOnPreDraw {
            startPostponedEnterTransition()
        }
    }

    open fun onViewCreatedCore(savedInstanceState: Bundle?){}

    /**
     * Retyped [androidx.fragment.app.Fragment.requireActivity]
     */
    @Suppress("UNCHECKED_CAST")
    override val typedActivity: A by lazy {
        requireActivity() as A
    }

    override val sharedViewModel: VM by lazy {
        ViewModelProvider(requireActivity())[viewModelClass]
    }
}
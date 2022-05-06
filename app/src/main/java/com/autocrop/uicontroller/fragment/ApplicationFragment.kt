package com.autocrop.uicontroller.fragment

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.autocrop.uicontroller.ViewModelHolder
import com.autocrop.utils.BlankFun

abstract class ApplicationFragment<A: Activity, VB: ViewBinding, VM: ViewModel>(
    viewModelClass: Class<VM>,
    bindingClass: Class<VB>):
        ViewBoundFragment<VB>(bindingClass),
        ViewModelHolder<VM> {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = withPostponedEnterTransition {
        super.onViewCreated(view, savedInstanceState)
        onViewCreatedCore(savedInstanceState)
    }

    open fun onViewCreatedCore(savedInstanceState: Bundle?){}

    /**
     * Retyped [androidx.fragment.app.Fragment.requireActivity]
     */
    @Suppress("UNCHECKED_CAST")
    val castedActivity: A by lazy {
        requireActivity() as A
    }

    override val sharedViewModel: VM by lazy {
        ViewModelProvider(requireActivity())[viewModelClass]
    }
}

private inline fun Fragment.withPostponedEnterTransition(f: BlankFun){
    postponeEnterTransition()
    f()
    startPostponedEnterTransition()
}
package com.w2sv.autocrop.controller.fragment

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.createViewModelLazy
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding
import com.w2sv.autocrop.controller.activity.FragmentHostingActivity
import com.w2sv.autocrop.controller.activity.retriever.FragmentHostingActivityRetriever
import com.w2sv.viewboundcontroller.ViewBoundFragment

abstract class ApplicationFragment<A : Activity, VB : ViewBinding, VM : ViewModel>(
    viewModelClass: Class<VM>,
    bindingClass: Class<VB>
) :
    ViewBoundFragment<VB>(bindingClass),
    FragmentHostingActivityRetriever {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        postponeEnterTransition()

        super.onViewCreated(view, savedInstanceState)
        onViewCreatedCore(savedInstanceState)

        (view.parent as? ViewGroup)?.doOnPreDraw {
            startPostponedEnterTransition()
        }
    }

    protected open fun onViewCreatedCore(savedInstanceState: Bundle?) {}

    protected val applicationViewModel: VM by createViewModelLazy(
        viewModelClass.kotlin,
        { requireActivity().viewModelStore }
    )

    @Suppress("UNCHECKED_CAST")
    protected val castActivity: A
        get() = requireActivity() as A

    override val fragmentHostingActivity: FragmentHostingActivity<*>
        get() = requireActivity() as FragmentHostingActivity<*>
}
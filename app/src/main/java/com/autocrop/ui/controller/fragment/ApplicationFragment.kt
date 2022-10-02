package com.autocrop.ui.controller.fragment

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.createViewModelLazy
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding
import com.autocrop.retriever.activity.CustomActivityRetriever
import com.autocrop.retriever.viewmodel.SharedViewModelRetriever
import com.autocrop.ui.controller.activity.FragmentHostingActivity
import kotlin.reflect.KClass

abstract class ApplicationFragment<A: Activity, VB: ViewBinding, VM: ViewModel>(
    viewModelKClass: KClass<VM>,
    bindingClass: Class<VB>):
        ViewBoundFragment<VB>(bindingClass),
        SharedViewModelRetriever<VM>,
        CustomActivityRetriever<A> {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        postponeEnterTransition()

        super.onViewCreated(view, savedInstanceState)
        onViewCreatedCore(savedInstanceState)

        (view.parent as? ViewGroup)?.doOnPreDraw {
            startPostponedEnterTransition()
        }
    }

    open fun onViewCreatedCore(savedInstanceState: Bundle?){}

    override val sharedViewModel: VM by createViewModelLazy(
        viewModelKClass,
        {requireActivity().viewModelStore}
    )

    //$$$$$$$$$$$$$$$$$$$$$$$$$$
    // CustomActivityRetriever $
    //$$$$$$$$$$$$$$$$$$$$$$$$$$
    @Suppress("UNCHECKED_CAST")
    override val typedActivity: A
        get() = requireActivity() as A

    override val fragmentHostingActivity: FragmentHostingActivity<*>
        get() = requireActivity() as FragmentHostingActivity<*>
}
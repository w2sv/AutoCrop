package com.autocrop.controller.fragment

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.createViewModelLazy
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding
import com.autocrop.controller.activity.FragmentHostingActivity
import com.autocrop.controller.activity.retriever.FragmentHostingActivityRetriever
import com.autocrop.controller.activity.retriever.TypedActivityRetriever
import com.w2sv.viewboundcontroller.ViewBoundFragment
import kotlin.reflect.KClass

abstract class ApplicationFragment<A: Activity, VB: ViewBinding, VM: ViewModel>(
    viewModelKClass: KClass<VM>,
    bindingClass: Class<VB>):
    ViewBoundFragment<VB>(bindingClass),
    TypedActivityRetriever<A>,
    FragmentHostingActivityRetriever {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        postponeEnterTransition()

        super.onViewCreated(view, savedInstanceState)
        onViewCreatedCore(savedInstanceState)

        (view.parent as? ViewGroup)?.doOnPreDraw {
            startPostponedEnterTransition()
        }
    }

    open fun onViewCreatedCore(savedInstanceState: Bundle?){}

    protected val sharedViewModel: VM by createViewModelLazy(
        viewModelKClass,
        {requireActivity().viewModelStore}
    )

    @Suppress("UNCHECKED_CAST")
    override val typedActivity: A
        get() = requireActivity() as A

    override val fragmentHostingActivity: FragmentHostingActivity<*>
        get() = requireActivity() as FragmentHostingActivity<*>
}
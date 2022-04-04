package com.autocrop.uicontroller.fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType

abstract class ViewBindingHandlingFragment<A: Activity, VM: ViewModel, VB: ViewBinding>(viewModelClass: Class<VM>)
        : SharedViewModelHoldingFragment<A, VM>(viewModelClass){

    private var _binding: VB? = null
    protected val binding: VB
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        _binding = inflateViewBinding(container)
        return binding.root
    }

    /**
     * @see
     *      https://stackoverflow.com/a/67395787/12083276
     */
    @Suppress("UNCHECKED_CAST")
    private fun inflateViewBinding(container: ViewGroup?): VB =
        ((javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<VB>)
            .getMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java)
            .invoke(null, layoutInflater, container, false) as VB

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
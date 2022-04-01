package com.autocrop.utils.android

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.viewbinding.ViewBinding

abstract class ExtendedFragment<A: Activity, M: ViewModel>(viewModelClass: Class<M>):
    Fragment(){

    protected val sharedViewModel: M by lazy { ViewModelProvider(activity as ViewModelStoreOwner)[viewModelClass] }

    /**
     * Retyped [androidx.fragment.app.Fragment.requireActivity]
     */
    @Suppress("UNCHECKED_CAST")
    val activity: A
        get() = requireActivity() as A
}

typealias InflateViewBinding<VB>  = (LayoutInflater, ViewGroup?, Boolean) -> VB

abstract class BindingHandlingFragment<A: Activity, VM: ViewModel, VB: ViewBinding>(
    viewModelClass: Class<VM>,
    private val inflateViewBinding: InflateViewBinding<VB>
)
    : ExtendedFragment<A, VM>(viewModelClass){

    private var _binding: VB? = null
    protected val binding: VB
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflateViewBinding(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.autocrop.uicontroller.fragment

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.viewbinding.ViewBinding

abstract class SharedViewModelAndViewBindingHoldingFragment<A: Activity, VB: ViewBinding, VM: ViewModel>(viewModelClass: Class<VM>)
        : ViewBindingHoldingFragment<A, VB>(){

    protected val sharedViewModel: VM by lazy {
        ViewModelProvider(requireActivity())[viewModelClass]
    }
}
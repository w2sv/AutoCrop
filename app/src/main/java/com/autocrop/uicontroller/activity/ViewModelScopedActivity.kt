package com.autocrop.uicontroller.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding

abstract class ViewModelScopedActivity<VB: ViewBinding, VM: ViewModel>(viewModelClass: Class<VM>)
    : EntrySnackbarDisplayingActivity<VB>(){

    protected val sharedViewModel: VM by lazy {
        ViewModelProvider(this, viewModelFactory() ?: defaultViewModelProviderFactory)[viewModelClass]
    }

    protected open fun viewModelFactory(): ViewModelProvider.Factory? = null
}
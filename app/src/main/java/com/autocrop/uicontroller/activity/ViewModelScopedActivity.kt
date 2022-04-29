package com.autocrop.uicontroller.activity

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding

abstract class ViewModelScopedActivity<VB: ViewBinding, VM: ViewModel>(private val viewModelClass: Class<VM>)
    : EntrySnackbarDisplayingActivity<VB>(){

    lateinit var sharedViewModel: VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedViewModel = ViewModelProvider(this, viewModelFactory())[viewModelClass]
    }

    protected open fun viewModelFactory(): ViewModelProvider.Factory = defaultViewModelProviderFactory
}
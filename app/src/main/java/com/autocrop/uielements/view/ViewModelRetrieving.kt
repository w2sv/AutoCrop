package com.autocrop.uielements.view

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

interface ViewModelRetriever<VM: ViewModel>{
    val viewModel: VM
}

abstract class AbstractContextBasedViewModelRetriever<VM: ViewModel, VMST: ViewModelStoreOwner>(context: Context, viewModelClass: Class<VM>)
    : ViewModelRetriever<VM> {

    @Suppress("UNCHECKED_CAST")
    override val viewModel: VM by lazy {
        ViewModelProvider(context as VMST)[viewModelClass]
    }
}
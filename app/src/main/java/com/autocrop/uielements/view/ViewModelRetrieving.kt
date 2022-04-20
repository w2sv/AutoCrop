package com.autocrop.uielements.view

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

interface ViewModelRetriever<VM: ViewModel>{
    val viewModel: VM
}

abstract class ViewModelRetrievingView<VM: ViewModel, VMSO: ViewModelStoreOwner>(context: Context, viewModelClass: Class<VM>)
    : ViewModelRetriever<VM> {

    /**
     * Retrieve instance of [VM] by means of context
     */
    @Suppress("UNCHECKED_CAST")
    override val viewModel: VM by lazy {
        ViewModelProvider(context as VMSO)[viewModelClass]
    }
}
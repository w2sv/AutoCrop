package com.autocrop.retriever.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

abstract class ContextBasedViewModelRetriever<VM: ViewModel, VMSO: ViewModelStoreOwner>(
    context: Context,
    viewModelClass: Class<VM>
)
        : ViewModelRetriever<VM> {

    /**
     * Retrieve instance of [VM] through converted context
     */
    @Suppress("UNCHECKED_CAST")
    override val sharedViewModel: VM by lazy {
        ViewModelProvider(context as VMSO)[viewModelClass]
    }
}
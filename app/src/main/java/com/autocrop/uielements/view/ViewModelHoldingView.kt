package com.autocrop.uielements.view

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.autocrop.uicontroller.ViewModelHolder

abstract class ViewModelHoldingView<VM: ViewModel, VMSO: ViewModelStoreOwner>(context: Context, viewModelClass: Class<VM>)
    : ViewModelHolder<VM> {

    /**
     * Retrieve instance of [VM] through converted context
     */
    @Suppress("UNCHECKED_CAST")
    override val sharedViewModel: VM by lazy {
        ViewModelProvider(context as VMSO)[viewModelClass]
    }
}
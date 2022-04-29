package com.autocrop.uicontroller.activity

import android.os.Bundle
import androidx.viewbinding.ViewBinding

abstract class EntrySnackbarDisplayingActivity<VB: ViewBinding>
    : ViewBindingHandlingActivity<VB>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null)
            displayEntrySnackbar()
    }

    protected open fun displayEntrySnackbar() {}

    protected fun <T> intentExtra(key: String, blacklistValue: T? = null): T? =
        intent.extras?.get(key).let {
            if (blacklistValue == null || it != blacklistValue)
                @Suppress("UNCHECKED_CAST")
                it as T
            else
                null
        }
}
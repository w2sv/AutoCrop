package com.autocrop.uicontroller.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.autocrop.uicontroller.ViewModelHolder

abstract class ApplicationActivity<RF: Fragment, VM: ViewModel>(
    rootFragmentClass: Class<RF>,
    viewModelClass: Class<VM>) :
        FragmentHostingActivity<RF>(rootFragmentClass),
        ViewModelHolder<VM>{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // instantiate sharedViewModel
        this::sharedViewModel.invoke()

        if (savedInstanceState == null)
            displayEntrySnackbar()
    }

    //$$$$$$$$$$$$$$$$$$
    // ViewModelHolder $
    //$$$$$$$$$$$$$$$$$$

    override val sharedViewModel: VM by lazy {
        ViewModelProvider(this, viewModelFactory())[viewModelClass]
    }

    protected open fun viewModelFactory(): ViewModelProvider.Factory =
        defaultViewModelProviderFactory

    //$$$$$$$$$$$$$$$$$$$$$$
    // Snackbar displaying $
    //$$$$$$$$$$$$$$$$$$$$$$

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
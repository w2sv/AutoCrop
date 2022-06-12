package com.autocrop.uicontroller.activity

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.autocrop.global.Preferences
import com.autocrop.global.preferencesInstances
import com.autocrop.retriever.viewmodel.ViewModelRetriever
import com.autocrop.utilsandroid.getApplicationWideSharedPreferences

abstract class ApplicationActivity<RF: Fragment, VM: ViewModel>(
    rootFragmentClass: Class<RF>,
    viewModelClass: Class<VM>,
    private val accessedPreferenceInstances: Array<Preferences<*>>? = null) :
        FragmentHostingActivity<RF>(rootFragmentClass),
    ViewModelRetriever<VM> {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // instantiate sharedViewModel
        ::sharedViewModel.invoke()

        if (savedInstanceState == null)
            onSavedInstanceStateNull()
    }

    protected open fun onSavedInstanceStateNull(){
        showEntrySnackbar()
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

    protected open fun showEntrySnackbar() {}

    protected fun <T> getIntentExtra(key: String, blacklistValue: T? = null): T? =
        intent.extras?.get(key).let {
            if (blacklistValue == null || it != blacklistValue)
                @Suppress("UNCHECKED_CAST")
                it as T
            else
                null
        }

    /**
     * Write changed values of each [preferencesInstances] element to SharedPreferences
     */
    override fun onStop() {
        super.onStop()

        val sharedPreferences = lazy { getApplicationWideSharedPreferences() }

        accessedPreferenceInstances?.forEach {
            it.writeChangedValuesToSharedPreferences(sharedPreferences)
        }
    }
}
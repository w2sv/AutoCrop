package com.autocrop.uicontroller.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider
import com.autocrop.activities.ActivityTransitions
import com.autocrop.activities.main.MainActivity
import com.autocrop.global.PreferencesArray
import com.autocrop.global.preferencesInstances
import com.autocrop.retriever.viewmodel.ViewModelRetriever
import com.autocrop.utilsandroid.getApplicationWideSharedPreferences
import kotlin.reflect.KClass

abstract class ApplicationActivity<RF: Fragment, VM: ViewModel>(
    rootFragmentClass: Class<RF>,
    viewModelKClass: KClass<VM>,
    private val accessedPreferenceInstances: PreferencesArray? = null) :
        FragmentHostingActivity<RF>(rootFragmentClass),
        ViewModelRetriever<VM>{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        instantiateSharedViewModel()

        if (savedInstanceState == null)
            onSavedInstanceStateNull()
    }

    private fun instantiateSharedViewModel(){
        ::sharedViewModel.invoke()
    }

    //$$$$$$$$$$$$$$$$$$$$$$
    // Snackbar displaying $
    //$$$$$$$$$$$$$$$$$$$$$$

    protected fun <T> getIntentExtra(key: String, blacklistValue: T? = null): T? =
        intent.extras?.get(key).let {
            if (blacklistValue == null || it != blacklistValue)
                @Suppress("UNCHECKED_CAST")
                it as T
            else
                null
        }

    //$$$$$$$$$$$$$$$$$$
    // ViewModelHolder $
    //$$$$$$$$$$$$$$$$$$

    override val sharedViewModel: VM by ViewModelLazy(
        viewModelKClass,
        {viewModelStore},
        ::viewModelFactory
    )

    protected open fun viewModelFactory(): ViewModelProvider.Factory =
        defaultViewModelProviderFactory

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

fun Activity.startMainActivity(withReturnAnimation: Boolean = true, intentApplier: ((Intent) -> Unit)? = null){
    startActivity(
        Intent(
            this,
            MainActivity::class.java
        )
            .apply {
                intentApplier?.invoke(this)
            }
    )
    if (withReturnAnimation)
        ActivityTransitions.RETURN(this)
}
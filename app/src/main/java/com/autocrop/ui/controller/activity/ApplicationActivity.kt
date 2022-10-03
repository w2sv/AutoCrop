package com.autocrop.ui.controller.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider
import com.autocrop.activities.main.MainActivity
import com.autocrop.preferences.TypedPreferences
import com.autocrop.retriever.viewmodel.SharedViewModelRetriever
import com.autocrop.utils.android.extensions.getApplicationWideSharedPreferences
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import kotlin.reflect.KClass

abstract class ApplicationActivity<RF: Fragment, VM: ViewModel>(
    rootFragmentClass: KClass<RF>,
    viewModelKClass: KClass<VM>,
    vararg val preferences: TypedPreferences<*>) :
        FragmentHostingActivity<RF>(rootFragmentClass.java),
        SharedViewModelRetriever<VM>{

    protected abstract val onBackPressedCallback: OnBackPressedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // instantiate sharedViewModel
        ::sharedViewModel.invoke()

        if (savedInstanceState == null)
            onSavedInstanceStateNull()

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    //$$$$$$$$$$$$$$$$$$$$$$
    // Snackbar displaying $
    //$$$$$$$$$$$$$$$$$$$$$$

    protected fun <T> getIntentExtra(key: String, blacklistValue: T? = null): T? =
        @Suppress("DEPRECATION")
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
     * Write changed values of each [preferences] element to SharedPreferences
     */
    override fun onStop() {
        super.onStop()

        val sharedPreferences = lazy { getApplicationWideSharedPreferences() }

        preferences.forEach {
            it.writeChangedValuesToSharedPreferences(sharedPreferences)
        }
    }

    fun startMainActivity(withReturnAnimation: Boolean = true, intentApplier: ((Intent) -> Unit)? = null){
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
            Animatoo.animateSwipeRight(this)
    }
}
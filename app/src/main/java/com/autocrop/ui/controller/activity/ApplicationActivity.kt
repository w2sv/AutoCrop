package com.autocrop.ui.controller.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.autocrop.activities.main.MainActivity
import com.autocrop.preferences.TypedPreferences
import com.autocrop.utils.android.extensions.getApplicationWideSharedPreferences
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import kotlin.reflect.KClass

abstract class ApplicationActivity<RF: Fragment, VM: ViewModel>(
    rootFragmentKClass: KClass<RF>,
    private val viewModelKClass: KClass<VM>,
    vararg val preferences: TypedPreferences<*>
): FragmentHostingActivity<RF>(rootFragmentKClass.java){

    protected lateinit var viewModel: VM

    protected open fun viewModelFactory(): ViewModelProvider.Factory =
        defaultViewModelProviderFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            viewModelFactory()
        )[viewModelKClass.java]

        if (savedInstanceState == null)
            onSavedInstanceStateNull()

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    protected abstract val onBackPressedCallback: OnBackPressedCallback

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
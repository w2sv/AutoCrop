package com.w2sv.autocrop.controller.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.w2sv.autocrop.activities.main.MainActivity
import com.w2sv.autocrop.preferences.TypedPreferences
import com.w2sv.autocrop.utils.android.extensions.getApplicationWideSharedPreferences

abstract class ApplicationActivity<RF : Fragment, VM : ViewModel>(
    rootFragmentKClass: Class<RF>,
    private val viewModelKClass: Class<VM>,
    vararg val preferences: TypedPreferences<*>
) : FragmentHostingActivity<RF>(rootFragmentKClass) {

    protected lateinit var viewModel: VM

    protected open fun viewModelFactory(): ViewModelProvider.Factory =
        defaultViewModelProviderFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(
            this,
            viewModelFactory()
        )[viewModelKClass]

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

    fun startMainActivity(withReturnAnimation: Boolean = true, applyToIntent: ((Intent) -> Intent)? = null) {
        startActivity(
            Intent(
                this,
                MainActivity::class.java
            )
                .apply {
                    applyToIntent?.invoke(this)
                }
        )
        if (withReturnAnimation)
            Animatoo.animateSwipeRight(this)
    }
}
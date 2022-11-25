package com.w2sv.autocrop.activities

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.w2sv.autocrop.preferences.BooleanPreferences
import com.w2sv.autocrop.preferences.UriPreferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class ApplicationActivity(
    rootFragmentClass: Class<out Fragment>
) : FragmentHostingActivity(rootFragmentClass) {

    @Inject
    lateinit var booleanPreferences: BooleanPreferences

    @Inject
    lateinit var uriPreferences: UriPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        listOf(booleanPreferences, uriPreferences).forEach {
            lifecycle.addObserver(it)
        }

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        if (savedInstanceState == null)
            launchRootFragment()
    }

    protected abstract val onBackPressedCallback: OnBackPressedCallback
}
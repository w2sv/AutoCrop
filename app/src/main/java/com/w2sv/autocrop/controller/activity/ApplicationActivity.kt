package com.w2sv.autocrop.controller.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.w2sv.autocrop.activities.main.MainActivity
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

        if (savedInstanceState == null)
            launchRootFragment()

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    protected abstract val onBackPressedCallback: OnBackPressedCallback

    fun startMainActivity(withReturnAnimation: Boolean = true, configureIntent: ((Intent) -> Intent)? = null) {
        startActivity(
            Intent(
                this,
                MainActivity::class.java
            )
                .apply {
                    configureIntent?.invoke(this)
                }
        )
        if (withReturnAnimation)
            Animatoo.animateSwipeRight(this)
    }
}
package com.w2sv.autocrop.activities

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import com.w2sv.autocrop.preferences.BooleanPreferences
import com.w2sv.autocrop.preferences.CropSaveDirPreferences
import com.w2sv.autocrop.preferences.Flags
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class ApplicationActivity : FragmentHostingActivity() {

    // TODO: only inject what's actually required by child activity

    @Inject
    lateinit var booleanPreferences: BooleanPreferences

    @Inject
    lateinit var cropSaveDirPreferences: CropSaveDirPreferences

    @Inject
    lateinit var flags: Flags

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerObserversAndCallbacks()

        if (savedInstanceState == null)
            launchRootFragment()
    }

    private fun registerObserversAndCallbacks(){
        listOf(booleanPreferences, cropSaveDirPreferences, flags).forEach {
            lifecycle.addObserver(it)
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    protected abstract val onBackPressedCallback: OnBackPressedCallback
}
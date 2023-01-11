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

    @Inject
    lateinit var booleanPreferences: BooleanPreferences

    @Inject
    lateinit var cropSaveDirPreferences: CropSaveDirPreferences

    @Inject
    lateinit var flags: Flags

    protected fun onCreateCore(savedInstanceState: Bundle?){
        listOf(booleanPreferences, cropSaveDirPreferences, flags).forEach {
            lifecycle.addObserver(it)
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        if (savedInstanceState == null)
            launchRootFragment()
    }

    protected abstract val onBackPressedCallback: OnBackPressedCallback
}
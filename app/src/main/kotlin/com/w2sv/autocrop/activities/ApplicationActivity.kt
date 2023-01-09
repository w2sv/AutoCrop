package com.w2sv.autocrop.activities

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import com.w2sv.autocrop.preferences.BooleanPreferences
import com.w2sv.autocrop.preferences.CropSaveDirPreferences
import com.w2sv.autocrop.preferences.ShownFlags
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class ApplicationActivity : FragmentHostingActivity() {

    @Inject
    lateinit var booleanPreferences: BooleanPreferences

    @Inject
    lateinit var cropSaveDirPreferences: CropSaveDirPreferences

    @Inject
    lateinit var shownFlags: ShownFlags

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        listOf(booleanPreferences, cropSaveDirPreferences, shownFlags).forEach {
            lifecycle.addObserver(it)
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        if (savedInstanceState == null)
            launchRootFragment()
    }

    protected abstract val onBackPressedCallback: OnBackPressedCallback
}
package com.w2sv.autocrop.activities.main

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleObserver
import com.w2sv.autocrop.activities.AppActivity
import com.w2sv.autocrop.activities.main.fragments.about.AboutFragment
import com.w2sv.autocrop.activities.main.fragments.flowfield.FlowFieldFragment
import com.w2sv.autocrop.domain.AccumulatedIOResults
import com.w2sv.autocrop.utils.extensions.addObservers
import com.w2sv.common.extensions.getParcelableExtraCompat
import com.w2sv.common.preferences.BooleanPreferences
import com.w2sv.common.preferences.CropSaveDirPreferences
import com.w2sv.common.preferences.GlobalFlags
import com.w2sv.common.preferences.IntPreferences
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppActivity() {

    companion object {
        const val EXTRA_SELECTED_IMAGE_URIS = "com.w2sv.autocrop.extra.SELECTED_IMAGE_URIS"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        addObservers(viewModels<ViewModel>().value.lifecycleObservers)

        supportFragmentManager.addOnBackStackChangedListener {
            when (val fragment = getCurrentFragment()) {
                is FlowFieldFragment -> fragment.binding.navigationViewToggleButton.progress = 0f
            }
        }
    }

    @HiltViewModel
    class ViewModel @Inject constructor(
        globalFlags: GlobalFlags,
        booleanPreferences: BooleanPreferences,
        cropSaveDirPreferences: CropSaveDirPreferences,
        intPreferences: IntPreferences
    ) : androidx.lifecycle.ViewModel() {

        val lifecycleObservers: List<LifecycleObserver> =
            listOf(globalFlags, booleanPreferences, cropSaveDirPreferences, intPreferences)
    }

    //////////////////////////////////////
    //   ApplicationActivity overrides  //
    //////////////////////////////////////

    override fun getRootFragment(): Fragment =
        FlowFieldFragment.getInstance(intent.getParcelableExtraCompat(AccumulatedIOResults.EXTRA))

    override fun handleOnBackPressed() {
        when (val fragment = getCurrentFragment()) {
            is AboutFragment -> supportFragmentManager.popBackStack()
            is FlowFieldFragment -> fragment.onBackPress()
            else -> Unit
        }
    }
}
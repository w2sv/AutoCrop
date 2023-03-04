package com.w2sv.autocrop.activities.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleObserver
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.w2sv.autocrop.activities.AppActivity
import com.w2sv.autocrop.activities.main.fragments.about.AboutFragment
import com.w2sv.autocrop.activities.main.fragments.flowfield.FlowFieldFragment
import com.w2sv.autocrop.domain.AccumulatedIOResults
import com.w2sv.common.extensions.getParcelableExtraCompat
import com.w2sv.preferences.BooleanPreferences
import com.w2sv.preferences.CropSaveDirPreferences
import com.w2sv.preferences.GlobalFlags
import com.w2sv.preferences.IntPreferences
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppActivity() {

    companion object {
        const val EXTRA_SELECTED_IMAGE_URIS = "com.w2sv.autocrop.extra.SELECTED_IMAGE_URIS"

        fun start(
            sourceActivity: Activity,
            clearPreviousActivity: Boolean = false,
            animation: ((Context) -> Unit)? = Animatoo::animateSwipeRight,
            configureIntent: (Intent.() -> Intent)? = null
        ) {
            sourceActivity.startActivity(
                Intent(
                    sourceActivity,
                    MainActivity::class.java
                )
                    .apply {
                        if (clearPreviousActivity) {
                            flags = FLAG_ACTIVITY_CLEAR_TASK
                        }
                        configureIntent?.invoke(this)
                    }
            )
            animation?.invoke(sourceActivity)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

    override val lifecycleObservers: List<LifecycleObserver> get() = viewModels<ViewModel>().value.lifecycleObservers

    override fun getRootFragment(): Fragment =
        FlowFieldFragment.getInstance(intent.getParcelableExtraCompat(AccumulatedIOResults.EXTRA))

    override fun handleOnBackPressed() {
        getCurrentFragment().let {
            when (it) {
                is AboutFragment -> supportFragmentManager.popBackStack()
                is FlowFieldFragment -> it.onBackPress()
                else -> Unit
            }
        }
    }
}
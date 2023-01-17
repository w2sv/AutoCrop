package com.w2sv.autocrop.activities.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleObserver
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.w2sv.autocrop.activities.ApplicationActivity
import com.w2sv.autocrop.activities.examination.AccumulatedIOResults
import com.w2sv.autocrop.activities.main.fragments.about.AboutFragment
import com.w2sv.autocrop.activities.main.fragments.flowfield.FlowFieldFragment
import com.w2sv.autocrop.preferences.BooleanPreferences
import com.w2sv.autocrop.preferences.CropSaveDirPreferences
import com.w2sv.autocrop.preferences.GlobalFlags
import com.w2sv.autocrop.utils.extensions.getParcelable
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ApplicationActivity() {

    companion object {
        const val EXTRA_SELECTED_IMAGE_URIS = "com.w2sv.autocrop.extra.SELECTED_IMAGE_URIS"

        fun start(
            activity: Activity,
            clearPreviousActivity: Boolean = false,
            animation: ((Context) -> Unit)? = Animatoo::animateSwipeRight,
            configureIntent: (Intent.() -> Intent)? = null
        ) {
            activity.startActivity(
                Intent(
                    activity,
                    MainActivity::class.java
                )
                    .apply {
                        if (clearPreviousActivity) {
                            flags = FLAG_ACTIVITY_CLEAR_TASK
                        }
                        configureIntent?.invoke(this)
                    }
            )
            animation?.invoke(activity)
        }
    }

    /**
     * ApplicationActivity overrides
     */

    @Inject
    lateinit var globalFlags: GlobalFlags
    @Inject
    lateinit var booleanPreferences: BooleanPreferences
    @Inject
    lateinit var cropSaveDirPreferences: CropSaveDirPreferences

    override val lifecycleObservers: List<LifecycleObserver>
        get() = listOf(globalFlags, booleanPreferences, cropSaveDirPreferences)

    override fun getRootFragment(): Fragment =
        FlowFieldFragment.getInstance(intent.getParcelable(AccumulatedIOResults.EXTRA))

    /**
     * invoke [FlowFieldFragment] if [AboutFragment] showing, otherwise exit app
     */
    override fun handleOnBackPressed() {
        getCurrentFragment().let {
            when (it) {
                is AboutFragment -> supportFragmentManager.popBackStack()
                is FlowFieldFragment -> {
                    it.binding.drawerLayout.run {
                        if (isOpen)
                            closeDrawer(GravityCompat.START)
                        else
                            it.onBackPress()
                    }
                }

                else -> Unit
            }
        }
    }
}
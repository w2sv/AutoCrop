package com.w2sv.autocrop.activities.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.w2sv.androidutils.os.getParcelableCompat
import com.w2sv.autocrop.activities.ViewBoundFragmentActivity
import com.w2sv.autocrop.activities.examination.IOResults
import com.w2sv.autocrop.activities.main.flowfield.FlowFieldFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ViewBoundFragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        supportFragmentManager.addOnBackStackChangedListener {
            when (val fragment = getCurrentFragment()) {
                is FlowFieldFragment -> fragment.binding.navigationViewToggleButton.progress = 0f
            }
        }
    }

    //////////////////////////////////////
    //   ApplicationActivity overrides  //
    //////////////////////////////////////

    override fun getRootFragment(): Fragment =
        FlowFieldFragment.getInstance(intent.getParcelableCompat(IOResults.EXTRA))

    companion object {
        const val EXTRA_SELECTED_IMAGE_URIS = "com.w2sv.autocrop.extra.SELECTED_IMAGE_URIS"

        fun start(
            activity: Activity,
            animation: ((Context) -> Unit)? = Animatoo::animateSwipeRight,
            configureIntent: (Intent.() -> Intent) = { this }
        ) {
            activity.startActivity(
                Intent(
                    activity,
                    MainActivity::class.java
                )
                    .configureIntent()
                    .apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP }
            )
            animation?.invoke(activity)
        }
    }
}
package com.w2sv.autocrop.activities.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.w2sv.androidutils.generic.getParcelableCompat
import com.w2sv.autocrop.activities.AppActivity
import com.w2sv.autocrop.activities.examination.IOResults
import com.w2sv.autocrop.activities.main.about.AboutFragment
import com.w2sv.autocrop.activities.main.flowfield.FlowFieldFragment
import dagger.hilt.android.AndroidEntryPoint
import slimber.log.i

@AndroidEntryPoint
class MainActivity : AppActivity() {

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

    override fun handleOnBackPressed() {
        i { "handleOnBackPressed" }

        when (val fragment = getCurrentFragment()) {
            is AboutFragment -> supportFragmentManager.popBackStack()
            is FlowFieldFragment -> fragment.onBackPress()
            else -> throw IllegalStateException("Invalid Fragment type")
        }
    }

    companion object {
        const val EXTRA_SELECTED_IMAGE_URIS = "com.w2sv.autocrop.extra.SELECTED_IMAGE_URIS"

        fun start(
            context: Context,
            animation: ((Context) -> Unit)? = Animatoo::animateSwipeRight,
            configureIntent: (Intent.() -> Intent) = { this }
        ) {
            context.startActivity(
                Intent(
                    context,
                    MainActivity::class.java
                )
                    .configureIntent()
                    .apply { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP }
            )
            animation?.invoke(context)
        }
    }
}
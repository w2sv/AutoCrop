package com.w2sv.autocrop.activities.main

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.w2sv.androidutils.extensions.postValue
import com.w2sv.autocrop.activities.ApplicationActivity
import com.w2sv.autocrop.activities.examination.IOResults
import com.w2sv.autocrop.activities.main.fragments.about.AboutFragment
import com.w2sv.autocrop.activities.main.fragments.flowfield.FlowFieldFragment
import com.w2sv.autocrop.screenshotlistening.ScreenshotListener
import com.w2sv.autocrop.utils.extensions.getParcelable
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ApplicationActivity() {

    companion object {
        const val EXTRA_SELECTED_IMAGE_URIS = "com.w2sv.autocrop.extra.SELECTED_IMAGE_URIS"

        fun restart(
            activity: Activity,
            withReturnAnimation: Boolean = true,
            configureIntent: Intent.() -> Intent = { this }
        ) {
            activity.startActivity(
                Intent(
                    activity,
                    MainActivity::class.java
                )
                    .configureIntent()
            )
            if (withReturnAnimation)
                Animatoo.animateSwipeRight(activity)
        }
    }

    class ViewModel : androidx.lifecycle.ViewModel() {
        val liveScreenshotListenerRunning: LiveData<Boolean?> by lazy {
            MutableLiveData()
        }
    }

    private val viewModel: ViewModel by viewModels()

    override fun getRootFragment(): Fragment =
        FlowFieldFragment.getInstance(intent.getParcelable(IOResults.EXTRA))

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        LocalBroadcastManager
            .getInstance(this)
            .registerReceiver(
                onCancelledScreenshotListenerFromNotificationListener,
                IntentFilter(ScreenshotListener.OnCancelledFromNotificationListener.ACTION_NOTIFY_ON_SCREENSHOT_LISTENER_CANCELLED_LISTENERS)
            )
    }

    private val onCancelledScreenshotListenerFromNotificationListener by lazy {
        OnCancelledScreenshotListenerFromNotificationListener()
    }

    inner class OnCancelledScreenshotListenerFromNotificationListener : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            viewModel
                .liveScreenshotListenerRunning
                .postValue(false)
        }
    }

    /**
     * invoke [FlowFieldFragment] if [AboutFragment] showing, otherwise exit app
     */
    override val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
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

    override fun onDestroy() {
        super.onDestroy()

        LocalBroadcastManager
            .getInstance(this)
            .unregisterReceiver(onCancelledScreenshotListenerFromNotificationListener)
    }
}
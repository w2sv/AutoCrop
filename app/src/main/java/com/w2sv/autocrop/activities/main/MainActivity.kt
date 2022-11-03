package com.w2sv.autocrop.activities.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.w2sv.autocrop.activities.iodetermination.IODeterminationActivity
import com.w2sv.autocrop.activities.main.fragments.about.AboutFragment
import com.w2sv.autocrop.activities.main.fragments.flowfield.FlowFieldFragment
import com.w2sv.autocrop.controller.activity.ApplicationActivity
import com.w2sv.autocrop.preferences.BooleanPreferences
import com.w2sv.autocrop.preferences.UriPreferences
import com.w2sv.autocrop.screenshotlistening.services.ScreenshotListener
import com.w2sv.autocrop.utils.android.extensions.postValue

class MainActivity :
    ApplicationActivity<FlowFieldFragment, MainActivityViewModel>(
        FlowFieldFragment::class.java,
        MainActivityViewModel::class.java,
        BooleanPreferences, UriPreferences
    ) {

    companion object {
        const val EXTRA_SELECTED_IMAGE_URIS = "com.w2sv.autocrop.extra.SELECTED_IMAGE_URIS"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        LocalBroadcastManager
            .getInstance(this)
            .registerReceiver(
                onStopScreenshotListenerFromNotification,
                IntentFilter(ScreenshotListener.OnStopFromNotificationListener.ACTION_ON_STOP_SERVICE_FROM_NOTIFICATION)
            )
    }

    private val onStopScreenshotListenerFromNotification by lazy {
        OnStopScreenshotListenerFromNotification()
    }

    inner class OnStopScreenshotListenerFromNotification : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            viewModel
                .cancelledScreenshotListenerFromNotification
                .postValue(true)
        }
    }

    override fun viewModelFactory(): ViewModelProvider.Factory =
        MainActivityViewModel.Factory(
            ioResults = IODeterminationActivity.Results.restore(intent)
        )

    /**
     * invoke [FlowFieldFragment] if [AboutFragment] showing, otherwise exit app
     */
    override val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            getCurrentFragment().let {
                if (it is AboutFragment)
                    return supportFragmentManager.popBackStack()
                (it as? FlowFieldFragment)?.binding?.drawerLayout?.run {
                    if (isOpen)
                        return closeDrawer(GravityCompat.START)
                }
                finishAffinity()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        LocalBroadcastManager
            .getInstance(this)
            .unregisterReceiver(onStopScreenshotListenerFromNotification)
    }
}
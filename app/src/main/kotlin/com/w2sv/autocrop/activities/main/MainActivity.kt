package com.w2sv.autocrop.activities.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.w2sv.androidutils.extensions.postValue
import com.w2sv.autocrop.activities.ApplicationActivity
import com.w2sv.autocrop.activities.cropexamination.CropExaminationActivity
import com.w2sv.autocrop.activities.main.fragments.about.AboutFragment
import com.w2sv.autocrop.activities.main.fragments.flowfield.FlowFieldFragment
import com.w2sv.autocrop.screenshotlistening.services.ScreenshotListener
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

class MainActivity :
    ApplicationActivity(FlowFieldFragment::class.java) {

    companion object {
        const val EXTRA_SELECTED_IMAGE_URIS = "com.w2sv.autocrop.extra.SELECTED_IMAGE_URIS"

        fun restart(context: Context, withReturnAnimation: Boolean = true, configureIntent: ((Intent) -> Intent)? = null) {
            context.startActivity(
                Intent(
                    context,
                    MainActivity::class.java
                )
                    .apply {
                        configureIntent?.invoke(this)
                    }
            )
            if (withReturnAnimation)
                Animatoo.animateSwipeRight(context)
        }
    }

    @HiltViewModel
    class ViewModel @Inject constructor(savedStateHandle: SavedStateHandle) : androidx.lifecycle.ViewModel() {

        val ioResults: CropExaminationActivity.Results? = CropExaminationActivity.Results.restore(savedStateHandle)

        val liveScreenshotListenerRunning: LiveData<Boolean?> by lazy {
            MutableLiveData()
        }
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

    private val viewModel: ViewModel by viewModels()

    inner class OnStopScreenshotListenerFromNotification : BroadcastReceiver() {
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
package com.w2sv.autocrop.activities.main

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.w2sv.androidutils.extensions.postValue
import com.w2sv.autocrop.activities.ApplicationActivity
import com.w2sv.autocrop.activities.examination.IOResults
import com.w2sv.autocrop.activities.main.fragments.about.AboutFragment
import com.w2sv.autocrop.activities.main.fragments.flowfield.FlowFieldFragment
import com.w2sv.autocrop.preferences.BooleanPreferences
import com.w2sv.autocrop.preferences.CropSaveDirPreferences
import com.w2sv.autocrop.preferences.Flags
import com.w2sv.autocrop.screenshotlistening.ScreenshotListener
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

    class ViewModel : androidx.lifecycle.ViewModel() {
        val liveScreenshotListenerRunning: LiveData<Boolean?> by lazy {
            MutableLiveData()
        }
    }

    private val viewModel: ViewModel by viewModels()

    @Inject
    lateinit var flags: Flags
    @Inject
    lateinit var booleanPreferences: BooleanPreferences
    @Inject
    lateinit var cropSaveDirPreferences: CropSaveDirPreferences

    override val lifecycleObservers: List<LifecycleObserver>
        get() = listOf(flags, booleanPreferences, cropSaveDirPreferences)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        LocalBroadcastManager
            .getInstance(this)
            .registerReceiver(
                onCancelledScreenshotListenerFromNotificationListener,
                IntentFilter(ScreenshotListener.OnCancelledFromNotificationListener.ACTION_NOTIFY_ON_SCREENSHOT_LISTENER_CANCELLED_LISTENERS)
            )
    }

    override fun getRootFragment(): Fragment =
        FlowFieldFragment.getInstance(intent.getParcelable(IOResults.EXTRA))

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
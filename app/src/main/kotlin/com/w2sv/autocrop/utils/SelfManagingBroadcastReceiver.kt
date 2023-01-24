package com.w2sv.autocrop.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.localbroadcastmanager.content.LocalBroadcastManager

/**
 * [BroadcastReceiver] registering itself upon instantiation and unregistering itself in [onDestroy]
 */
abstract class SelfManagingBroadcastReceiver(
    lifecycleOwner: LifecycleOwner,
    intentFilter: IntentFilter
) : BroadcastReceiver(),
    DefaultLifecycleObserver {

    init {
        @Suppress("LeakingThis")
        LocalBroadcastManager
            .getInstance(lifecycleOwner.requireContext())
            .registerReceiver(
                this,
                intentFilter
            )
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)

        LocalBroadcastManager
            .getInstance(owner.requireContext())
            .unregisterReceiver(this)
    }
}

private fun LifecycleOwner.requireContext(): Context =
    (this as? Context)
        ?: (this as Fragment).requireContext()
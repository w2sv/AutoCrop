package com.w2sv.autocrop.utils

import android.content.BroadcastReceiver
import android.content.IntentFilter
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.localbroadcastmanager.content.LocalBroadcastManager

/**
 * [BroadcastReceiver] registering itself upon instantiation and unregistering itself in [onDestroy]
 */
abstract class SelfManagingBroadcastReceiver(
    private val broadcastManager: LocalBroadcastManager,
    intentFilter: IntentFilter
) : BroadcastReceiver(),
    DefaultLifecycleObserver {

    init {
        @Suppress("LeakingThis")
        broadcastManager
            .registerReceiver(
                this,
                intentFilter
            )
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)

        broadcastManager
            .unregisterReceiver(this)
    }
}
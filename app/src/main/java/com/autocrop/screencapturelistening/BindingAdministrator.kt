package com.autocrop.screencapturelistening

import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.autocrop.screencapturelistening.services.abstractservices.BoundService
import com.autocrop.utils.kotlin.delegates.Consumable
import timber.log.Timber

class BindingAdministrator<T : BoundService>(
    context: Context,
    val serviceClass: Class<T>
) : ContextWrapper(context) {
    private var boundService: T? = null
    private var onServiceConnected by Consumable<(T) -> Unit>(null)

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Timber.i("$name connected")
            boundService = (service as BoundService.LocalBinder).getService()
            onServiceConnected?.invoke(boundService!!)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            boundService = null
        }
    }

    fun callOnBoundService(function: (T) -> Unit) {
        if (boundService == null) {
            onServiceConnected = function
            bindService(
                Intent(this, serviceClass),
                serviceConnection,
                BIND_AUTO_CREATE
            )
        }
        else
            function(boundService!!)
    }

    fun unbindService() {
        unbindService(serviceConnection)
    }
}
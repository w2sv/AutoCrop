package com.w2sv.autocrop.screenshotlistening.services

import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.w2sv.autocrop.screenshotlistening.services.abstrct.BoundService
import com.w2sv.kotlinutils.delegates.Consumable
import slimber.log.i

open class ServiceBindingAdministrator<T : BoundService>(
    context: Context,
    private val serviceClass: Class<T>
) : ContextWrapper(context) {

    private var boundService: T? = null
    private var onServiceConnected by Consumable<(T) -> Unit>(null)

    private val serviceConnection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            i { "$name connected" }
            boundService = (service as BoundService.LocalBinder).getService()
            onServiceConnected?.invoke(boundService!!)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            boundService = null
        }
    }

    fun callOnBoundService(block: (T) -> Unit) {
        if (boundService == null) {
            onServiceConnected = block
            bindService(
                Intent(this, serviceClass),
                serviceConnection,
                BIND_AUTO_CREATE
            )
        }
        else
            block(boundService!!)
    }

    fun unbindService() {
        unbindService(serviceConnection)
        i { "Unbound $serviceClass" }
    }
}
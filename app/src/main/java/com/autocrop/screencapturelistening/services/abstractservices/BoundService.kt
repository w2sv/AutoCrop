package com.autocrop.screencapturelistening.services.abstractservices

import android.content.Intent
import android.os.Binder
import android.os.IBinder

abstract class BoundService : LoggingService() {

    inner class LocalBinder : Binder() {
        @Suppress("UNCHECKED_CAST")
        fun <T : BoundService> getService(): T =
            (this@BoundService) as T
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder =
        binder
}
package com.autocrop.screencapturelistening.abstractservices

import android.app.Service
import android.content.Intent
import timber.log.Timber

abstract class LoggingService: Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.i("${this::class.java.name}.onStartCommand; startId $startId")
        return super.onStartCommand(intent, flags, startId)
    }
}
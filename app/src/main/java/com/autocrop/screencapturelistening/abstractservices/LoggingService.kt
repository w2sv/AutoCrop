package com.autocrop.screencapturelistening.abstractservices

import android.app.Service
import android.content.Intent
import de.paul_woitaschek.slimber.i

abstract class LoggingService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        i { "${this::class.java.simpleName}.onStartCommand; startId $startId" }
        return super.onStartCommand(intent, flags, startId)
    }
}
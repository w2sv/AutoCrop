package com.w2sv.autocrop.screenshotlistening.services.abstrct

import android.app.Service
import android.content.Intent
import slimber.log.i

abstract class LoggingService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        i { "${this::class.java.simpleName}.onStartCommand; startId $startId" }
        return super.onStartCommand(intent, flags, startId)
    }
}
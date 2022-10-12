package com.autocrop.screencapturelistening.services.abstractservices

import android.content.Intent
import android.os.IBinder

abstract class UnboundService: LoggingService(){
    override fun onBind(intent: Intent?): IBinder? = null
}
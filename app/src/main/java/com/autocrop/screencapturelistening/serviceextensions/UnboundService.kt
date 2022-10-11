package com.autocrop.screencapturelistening.serviceextensions

import android.app.Service
import android.content.Intent
import android.os.IBinder

abstract class UnboundService: Service(){
    override fun onBind(intent: Intent?): IBinder? = null
}
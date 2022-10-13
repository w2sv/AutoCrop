package com.autocrop.screencapturelistening.services.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class StopScreenCaptureListeningServiceBroadcastReceiver: BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        context!!.stopService(
            Intent(context, ScreenCaptureListeningService::class.java)
        )
    }
}
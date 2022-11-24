package com.w2sv.autocrop.screenshotlistening.services.abstrct

import android.content.Intent
import android.os.IBinder

abstract class UnboundService : LoggingService() {
    override fun onBind(intent: Intent?): IBinder? =
        null
}
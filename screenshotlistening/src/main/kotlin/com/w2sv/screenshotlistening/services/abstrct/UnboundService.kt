package com.w2sv.screenshotlistening.services.abstrct

import android.content.Intent
import android.os.IBinder
import com.w2sv.screenshotlistening.services.abstrct.LoggingService

abstract class UnboundService : LoggingService() {

    override fun onBind(intent: Intent?): IBinder? =
        null
}
package com.w2sv.screenshotlistening.services.abstrct

import android.app.Service
import android.content.Intent
import slimber.log.i

abstract class LoggingService : Service() {

    fun emitOnStartCommandLog(intent: Intent?, flags: Int, startId: Int) {
        i {
            "onStartCommand; " +
                    "intent: $intent | " +
                    "flags: $flags | " +
                    "startId: $startId"
        }
    }

    fun emitOnReceiveLog(intent: Intent?) {
        i {
            "onReceive; intent: $intent"
        }
    }
}
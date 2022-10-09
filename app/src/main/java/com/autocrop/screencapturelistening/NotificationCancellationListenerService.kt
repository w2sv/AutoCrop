package com.autocrop.screencapturelistening

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.autocrop.utils.android.extensions.notificationManager
import com.autocrop.utils.android.extensions.showGroupUpdatedNotification
import com.autocrop.utils.kotlin.extensions.nonZeroOrdinal

class NotificationCancellationListenerService: Service(){
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ScreenCaptureListeningService.ids.remove(intent!!.getIntExtra(NOTIFICATION_ID_EXTRA_KEY, -1))

        if (ScreenCaptureListeningService.ids.size == 1) {
            with(notificationManager()) {
                val (id, builder) = ScreenCaptureListeningService.ids.element()
                showGroupUpdatedNotification(
                    id,
                    builder,
                    null
                )
                cancel(ScreenCaptureListeningService.groupNotificationId.nonZeroOrdinal)
            }
        }

        stopSelf()
        return START_REDELIVER_INTENT
    }
}
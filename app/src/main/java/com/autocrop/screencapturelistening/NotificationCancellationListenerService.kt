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
        // Remove notification from [ScreenCaptureListeningService.notifications]
        ScreenCaptureListeningService.notifications.remove(intent!!.getIntExtra(NOTIFICATION_ID_EXTRA_KEY, -1))

        // If subsequently only one notification left, disassociate it from group and
        // cancel group summary
        if (ScreenCaptureListeningService.notifications.size == 1) {
            with(notificationManager()) {
                showGroupUpdatedNotification(
                    ScreenCaptureListeningService.notifications.element(),
                    null
                )
                cancel(ScreenCaptureListeningService.groupNotificationId.nonZeroOrdinal)
            }
        }

        stopSelf()
        return START_REDELIVER_INTENT
    }
}
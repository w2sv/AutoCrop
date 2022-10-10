package com.autocrop.screencapturelistening

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber

class NotificationCancellationBroadcastReceiver: BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        val cancelledNotificationId = intent!!.getIntExtra(NOTIFICATION_ID_EXTRA_KEY, -1)
        ScreenCaptureListeningService.notifications.remove(cancelledNotificationId)
        Timber.i("Removed notification id $cancelledNotificationId")
        Timber.i("New size: ${ScreenCaptureListeningService.notifications.size}")

        // If subsequently only one notification left, disassociate it from group and
        // cancel group summary
//        if (ScreenCaptureListeningService.notifications.size == 1) {
//            with(context!!) {
//                showGroupUpdatedNotification(
//                    ScreenCaptureListeningService.notifications.element(),
//                    null
//                )
//                notificationManager().cancel(ScreenCaptureListeningService.groupNotificationId.nonZeroOrdinal)
//            }
//            Timber.i("Called onSingleNotificationLeft")
//        }
    }
}
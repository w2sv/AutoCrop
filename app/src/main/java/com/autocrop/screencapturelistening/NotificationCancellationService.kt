package com.autocrop.screencapturelistening

import android.content.Intent
import com.autocrop.screencapturelistening.abstractservices.UnboundService
import com.autocrop.screencapturelistening.notification.ASSOCIATED_NOTIFICATION_ID
import com.autocrop.screencapturelistening.notification.CANCEL_NOTIFICATION_ACTION
import com.autocrop.utils.android.extensions.notificationManager
import timber.log.Timber

class NotificationCancellationService : UnboundService() {
    private val bindingAdministrator = BindingAdministrator(
        this,
        ScreenCaptureListeningService::class.java
    )

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        PendingIntentRequestCode.notificationCancellationService.remove(startId)

        val notificationId = intent!!.process()

        bindingAdministrator.callOnBoundService {
            it.notificationGroup.onChildNotificationCancelled(notificationId)
        }

        return START_REDELIVER_INTENT
    }

    private fun Intent.process(): Int{
        val notificationId = getIntExtra(ASSOCIATED_NOTIFICATION_ID, -1)
        action?.let {
            if (it == CANCEL_NOTIFICATION_ACTION)
                notificationManager().cancel(notificationId)
                    .also { Timber.i("Cancelled notification $notificationId") }
        }
        return notificationId
    }

    override fun onDestroy() {
        super.onDestroy()

        unbindService(bindingAdministrator.serviceConnection)
    }
}
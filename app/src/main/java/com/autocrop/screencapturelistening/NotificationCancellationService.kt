package com.autocrop.screencapturelistening

import android.content.Intent
import com.autocrop.screencapturelistening.abstractservices.UnboundService
import com.autocrop.screencapturelistening.notification.ASSOCIATED_NOTIFICATION_ID
import com.autocrop.screencapturelistening.notification.ASSOCIATED_PENDING_REQUEST_CODES
import com.autocrop.screencapturelistening.notification.CANCEL_NOTIFICATION_ACTION
import com.autocrop.utils.android.extensions.getInt
import com.autocrop.utils.android.extensions.notificationManager
import timber.log.Timber

class NotificationCancellationService : UnboundService() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        with(intent!!){
            val notificationId = getInt(ASSOCIATED_NOTIFICATION_ID)
            action?.let {
                if (it == CANCEL_NOTIFICATION_ACTION)
                    notificationManager().cancel(notificationId)
                        .also { Timber.i("Cancelled notification $notificationId") }
            }

            bindingAdministrator.callOnBoundService {
                it.notificationGroup.onChildNotificationCancelled(notificationId)
                it.cancellationRequestCodes.removeAll(
                    getIntegerArrayListExtra(ASSOCIATED_PENDING_REQUEST_CODES)!!
                        .toSet()
                )
            }
        }
        return START_REDELIVER_INTENT
    }

    private val bindingAdministrator = BindingAdministrator(
        this,
        ScreenCaptureListeningService::class.java
    )

    override fun onDestroy() {
        super.onDestroy()

        bindingAdministrator.unbindService()
    }
}
package com.autocrop.screencapturelistening

import android.content.Intent
import com.autocrop.screencapturelistening.abstractservices.BoundService
import com.autocrop.screencapturelistening.abstractservices.UnboundService
import com.autocrop.screencapturelistening.notification.ASSOCIATED_NOTIFICATION_ID
import com.autocrop.screencapturelistening.notification.ASSOCIATED_PENDING_REQUEST_CODES
import com.autocrop.screencapturelistening.notification.CANCEL_NOTIFICATION
import com.autocrop.utils.android.extensions.getInt
import com.autocrop.utils.android.extensions.notificationManager
import timber.log.Timber

class NotificationCancellationService : UnboundService() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        with(intent!!){
            val notificationId = getInt(ASSOCIATED_NOTIFICATION_ID)
            if (getBooleanExtra(CANCEL_NOTIFICATION, false))
                notificationManager()
                    .cancel(notificationId)
                    .also { Timber.i("Cancelled notification $notificationId") }

            val cancellationClient = getStringExtra(BoundService.CANCELLATION_CLIENT)!!
            bindingAdministrators.first {it.cancellationClientIdentifier == cancellationClient}
                .also { Timber.i("Cancelling $cancellationClient") }
                .callOnBoundService {
                    it.notificationGroup.onChildNotificationCancelled(notificationId)
                    it.cancellationRequestCodes.removeAll(
                        getIntegerArrayListExtra(ASSOCIATED_PENDING_REQUEST_CODES)!!
                            .toSet()
                    )
                    it.onCancellation(this)
                }
            }
        return START_REDELIVER_INTENT
    }

    private val bindingAdministrators = arrayOf(
        BindingAdministrator(
            this,
            ScreenCaptureListeningService::class.java
        ),
        BindingAdministrator(
            this,
            CropIOService::class.java
        ),
    )

    override fun onDestroy() {
        super.onDestroy()

        bindingAdministrators.forEach {
            it.unbindService()
        }
    }
}
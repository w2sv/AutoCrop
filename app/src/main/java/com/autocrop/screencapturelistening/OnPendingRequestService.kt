package com.autocrop.screencapturelistening

import android.content.Intent
import com.autocrop.screencapturelistening.abstractservices.UnboundService
import com.autocrop.screencapturelistening.notification.ASSOCIATED_NOTIFICATION_ID
import com.autocrop.screencapturelistening.notification.ASSOCIATED_PENDING_REQUEST_CODES
import com.autocrop.screencapturelistening.notification.CANCEL_NOTIFICATION
import com.autocrop.screencapturelistening.notification.NotificationGroup
import com.autocrop.screencapturelistening.notification.PendingIntentRequestCodes
import com.autocrop.utils.android.extensions.getInt
import com.autocrop.utils.android.extensions.notificationManager
import timber.log.Timber

class OnPendingRequestService : UnboundService() {
    interface ClientInterface {
        companion object {
            const val CLIENT = "CANCELLATION_CLIENT"
        }

        val clientName: String
        val notificationGroup: NotificationGroup?
        val requestCodes: PendingIntentRequestCodes
        fun onCancellation(intent: Intent) {}
    }

    class Client(uniqueClientNumber: Int, 
                 override val notificationGroup: NotificationGroup? = null) : ClientInterface {
        override val clientName: String = this::class.java.name
        override val requestCodes: PendingIntentRequestCodes = PendingIntentRequestCodes(uniqueClientNumber * 100)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        with(intent!!){
            val notificationId = getInt(ASSOCIATED_NOTIFICATION_ID)
            if (getBooleanExtra(CANCEL_NOTIFICATION, false))
                notificationManager()
                    .cancel(notificationId)
                    .also { Timber.i("Cancelled notification $notificationId") }

            bindingAdministrators.first {
                it.serviceClass.name == getStringExtra(ClientInterface.CLIENT)!!
            }
                .callOnBoundService {boundService ->
                    (boundService as ClientInterface).let {
                        it.notificationGroup!!.onChildNotificationCancelled(notificationId)
                        it.requestCodes.removeAll(
                            getIntegerArrayListExtra(ASSOCIATED_PENDING_REQUEST_CODES)!!
                                .toSet()
                        )
                        it.onCancellation(this)
                    }
                }
            }
        return START_REDELIVER_INTENT
    }

    private val bindingAdministrators = arrayOf(
        BindingAdministrator(this, ScreenCaptureListeningService::class.java),
        BindingAdministrator(this, CropIOService::class.java)
    )

    override fun onDestroy() {
        super.onDestroy()

        bindingAdministrators.forEach {
            it.unbindService()
        }
    }
}
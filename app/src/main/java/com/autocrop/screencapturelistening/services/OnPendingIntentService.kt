package com.autocrop.screencapturelistening.services

import android.content.Intent
import com.autocrop.screencapturelistening.ASSOCIATED_NOTIFICATION_ID
import com.autocrop.screencapturelistening.ASSOCIATED_PENDING_REQUEST_CODES
import com.autocrop.screencapturelistening.BindingAdministrator
import com.autocrop.screencapturelistening.CANCEL_NOTIFICATION
import com.autocrop.screencapturelistening.PendingIntentRequestCodes
import com.autocrop.screencapturelistening.services.abstractservices.UnboundService
import com.autocrop.screencapturelistening.notifications.NotificationGroup
import com.autocrop.utils.android.extensions.getInt
import com.autocrop.utils.android.extensions.notificationManager
import timber.log.Timber

class OnPendingIntentService : UnboundService() {
    interface ClientInterface {
        companion object {
            const val CLIENT_INDEX = "CLIENT_INDEX"
        }

        val clientIndex: Int
        val notificationGroup: NotificationGroup?
        val requestCodes: PendingIntentRequestCodes
        fun onCancellation(intent: Intent) {}

        fun Intent.putClientExtras(notificationId: Int, associatedRequestCodes: ArrayList<Int>): Intent =
            this
                .putExtra(CLIENT_INDEX, clientIndex)
                .putExtra(ASSOCIATED_NOTIFICATION_ID, notificationId)
                .putIntegerArrayListExtra(ASSOCIATED_PENDING_REQUEST_CODES, associatedRequestCodes)
    }

    class Client(override val clientIndex: Int,
                 override val notificationGroup: NotificationGroup? = null) : ClientInterface {
        override val requestCodes: PendingIntentRequestCodes = PendingIntentRequestCodes(clientIndex * 100)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        with(intent!!){
            val notificationId = getInt(ASSOCIATED_NOTIFICATION_ID)
            if (getBooleanExtra(CANCEL_NOTIFICATION, false))
                notificationManager()
                    .cancel(notificationId)
                    .also { Timber.i("Cancelled notification $notificationId") }

            bindingAdministrators[getInt(ClientInterface.CLIENT_INDEX)]
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
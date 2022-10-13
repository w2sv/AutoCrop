package com.autocrop.screencapturelistening.services

import android.content.Intent
import com.autocrop.screencapturelistening.BindingAdministrator
import com.autocrop.screencapturelistening.PendingIntentRequestCodes
import com.autocrop.screencapturelistening.abstractservices.UnboundService
import com.autocrop.screencapturelistening.notifications.NotificationGroup
import com.autocrop.screencapturelistening.services.crop_io.CropIOService
import com.autocrop.utils.android.extensions.getInt
import com.autocrop.utils.android.extensions.notificationManager
import timber.log.Timber

class OnPendingIntentService : UnboundService() {
    companion object{
        const val EXTRA_ASSOCIATED_NOTIFICATION_ID = "com.autocrop.ASSOCIATED_NOTIFICATION_ID"
        const val EXTRA_CANCEL_NOTIFICATION = "com.autocrop.CANCEL_NOTIFICATION"
        const val EXTRA_ASSOCIATED_PENDING_REQUEST_CODES = "com.autocrop.ASSOCIATED_PENDING_REQUEST_CODES"
    }

    interface ClientInterface {
        companion object {
            const val CLIENT_INDEX = "CLIENT_INDEX"
        }

        val clientIndex: Int
        val notificationGroup: NotificationGroup?
        val requestCodes: PendingIntentRequestCodes
        fun onPendingIntentService(intent: Intent) {}

        fun Intent.putClientExtras(notificationId: Int, associatedRequestCodes: ArrayList<Int>): Intent =
            this
                .putExtra(CLIENT_INDEX, clientIndex)
                .putExtra(EXTRA_ASSOCIATED_NOTIFICATION_ID, notificationId)
                .putIntegerArrayListExtra(EXTRA_ASSOCIATED_PENDING_REQUEST_CODES, associatedRequestCodes)
    }

    class Client(override val clientIndex: Int,
                 override val notificationGroup: NotificationGroup? = null) : ClientInterface {
        override val requestCodes: PendingIntentRequestCodes = PendingIntentRequestCodes(clientIndex * 100)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        with(intent!!){
            val notificationId = getInt(EXTRA_ASSOCIATED_NOTIFICATION_ID)
            if (getBooleanExtra(EXTRA_CANCEL_NOTIFICATION, false))
                notificationManager()
                    .cancel(notificationId)
                    .also { Timber.i("Cancelled notification $notificationId") }

            bindingAdministrators[getInt(ClientInterface.CLIENT_INDEX)]
                .callOnBoundService {boundService ->
                    (boundService as ClientInterface).let {
                        it.notificationGroup!!.onChildNotificationCancelled(notificationId)
                        it.requestCodes.removeAll(
                            getIntegerArrayListExtra(EXTRA_ASSOCIATED_PENDING_REQUEST_CODES)!!
                                .toSet()
                        )
                        it.onPendingIntentService(this)
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
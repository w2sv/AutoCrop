package com.autocrop.screenshotlistening.services

import android.content.Intent
import com.autocrop.screenshotlistening.BindingAdministrator
import com.autocrop.screenshotlistening.PendingIntentRequestCodes
import com.autocrop.screenshotlistening.services.abstrct.UnboundService
import com.autocrop.screenshotlistening.notifications.NotificationGroup
import com.autocrop.utils.android.extensions.getInt
import com.autocrop.utils.android.extensions.notificationManager
import de.paul_woitaschek.slimber.i

class OnPendingIntentService : UnboundService() {
    companion object {
        const val EXTRA_ASSOCIATED_NOTIFICATION_ID = "com.autocrop.ASSOCIATED_NOTIFICATION_ID"
        const val EXTRA_CANCEL_NOTIFICATION = "com.autocrop.CANCEL_NOTIFICATION"
        const val EXTRA_ASSOCIATED_PENDING_REQUEST_CODES = "com.autocrop.ASSOCIATED_PENDING_REQUEST_CODES"
    }

    interface ClientInterface {
        companion object {
            const val EXTRA_CLIENT_INDEX = "com.autocrop.CLIENT_INDEX"
        }

        val clientIndex: Int
        val notificationGroup: NotificationGroup?
        val requestCodes: PendingIntentRequestCodes
        fun onPendingIntentService(intent: Intent) {}

        fun Intent.putOnPendingIntentServiceClientExtras(
            notificationId: Int,
            associatedRequestCodes: ArrayList<Int>,
            putCancelNotificationExtra: Boolean = false
        ): Intent =
            this
                .putExtra(EXTRA_CLIENT_INDEX, clientIndex)
                .putExtra(EXTRA_ASSOCIATED_NOTIFICATION_ID, notificationId)
                .putIntegerArrayListExtra(EXTRA_ASSOCIATED_PENDING_REQUEST_CODES, associatedRequestCodes)
                .apply {
                    if (putCancelNotificationExtra)
                        putExtra(EXTRA_CANCEL_NOTIFICATION, true)
                }
    }

    class Client(
        override val clientIndex: Int,
        override val notificationGroup: NotificationGroup? = null
    ) : ClientInterface {
        override val requestCodes: PendingIntentRequestCodes = PendingIntentRequestCodes(clientIndex * 100)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        with(intent!!) {
            val notificationId = getInt(EXTRA_ASSOCIATED_NOTIFICATION_ID)
            if (getBooleanExtra(EXTRA_CANCEL_NOTIFICATION, false))
                notificationManager()
                    .cancel(notificationId)
                    .also { i { "Cancelled notification $notificationId" } }

            bindingAdministrators[getInt(ClientInterface.EXTRA_CLIENT_INDEX)]
                .callOnBoundService { boundService ->
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
        BindingAdministrator(this, ScreenshotListener::class.java),
        BindingAdministrator(this, CropIOService::class.java)
    )

    override fun onDestroy() {
        super.onDestroy()

        bindingAdministrators.forEach {
            it.unbindService()
        }
    }
}
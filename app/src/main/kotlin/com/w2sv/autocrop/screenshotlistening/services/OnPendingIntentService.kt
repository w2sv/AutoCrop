package com.w2sv.autocrop.screenshotlistening.services

import android.content.Intent
import com.w2sv.androidutils.extensions.notificationManager
import com.w2sv.autocrop.screenshotlistening.BindingAdministrator
import com.w2sv.autocrop.screenshotlistening.PendingIntentRequestCodes
import com.w2sv.autocrop.screenshotlistening.notifications.NotificationGroup
import com.w2sv.autocrop.screenshotlistening.services.abstrct.UnboundService
import com.w2sv.autocrop.utils.extensions.getInt
import slimber.log.i

class OnPendingIntentService : UnboundService() {

    companion object {
        const val EXTRA_ASSOCIATED_NOTIFICATION_ID = "com.w2sv.autocrop.ASSOCIATED_NOTIFICATION_ID"
        const val EXTRA_CANCEL_NOTIFICATION = "com.w2sv.autocrop.CANCEL_NOTIFICATION"
        const val EXTRA_ASSOCIATED_PENDING_REQUEST_CODES = "com.w2sv.autocrop.ASSOCIATED_PENDING_REQUEST_CODES"
    }

    interface Client {

        companion object {
            const val EXTRA_CLIENT_INDEX = "com.w2sv.autocrop.CLIENT_INDEX"
        }

        val clientIndex: Int
        val notificationGroup: NotificationGroup
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

        class Impl(
            override val clientIndex: Int,
            override val notificationGroup: NotificationGroup
        ) : Client {

            override val requestCodes = PendingIntentRequestCodes(clientIndex)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificationId = intent!!.getInt(EXTRA_ASSOCIATED_NOTIFICATION_ID)

        if (intent.getBooleanExtra(EXTRA_CANCEL_NOTIFICATION, false))
            notificationManager()
                .cancel(notificationId)
                .also { i { "Cancelled notification $notificationId" } }

        bindingAdministrators[intent.getInt(Client.EXTRA_CLIENT_INDEX)]
            .callOnBoundService { boundService ->
                (boundService as Client).let {
                    it.notificationGroup!!.onChildNotificationCancelled(notificationId)
                    it.requestCodes.removeAll(
                        intent.getIntegerArrayListExtra(EXTRA_ASSOCIATED_PENDING_REQUEST_CODES)!!
                            .toSet()
                    )
                    it.onPendingIntentService(intent)
                }
            }
        return START_REDELIVER_INTENT
    }

    private val bindingAdministrators = arrayOf(
        BindingAdministrator(this, ScreenshotListener::class.java),
    )

    override fun onDestroy() {
        super.onDestroy()

        bindingAdministrators.forEach {
            it.unbindService()
        }
    }
}
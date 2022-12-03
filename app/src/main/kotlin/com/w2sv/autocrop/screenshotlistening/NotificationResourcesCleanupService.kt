package com.w2sv.autocrop.screenshotlistening

import android.content.Context
import android.content.Intent
import com.w2sv.androidutils.extensions.notificationManager
import com.w2sv.autocrop.screenshotlistening.notifications.NotificationGroup
import com.w2sv.autocrop.screenshotlistening.notifications.PendingIntentRequestCodes
import com.w2sv.autocrop.screenshotlistening.services.ServiceBindingAdministrator
import com.w2sv.autocrop.screenshotlistening.services.abstrct.BoundService
import com.w2sv.autocrop.screenshotlistening.services.abstrct.UnboundService
import com.w2sv.autocrop.utils.extensions.getInt
import slimber.log.i

class NotificationResourcesCleanupService : UnboundService() {

    companion object {
        const val EXTRA_ASSOCIATED_NOTIFICATION_ID = "com.w2sv.autocrop.ASSOCIATED_NOTIFICATION_ID"
        const val EXTRA_CANCEL_NOTIFICATION = "com.w2sv.autocrop.CANCEL_NOTIFICATION"
        const val EXTRA_ASSOCIATED_PENDING_REQUEST_CODES = "com.w2sv.autocrop.ASSOCIATED_PENDING_REQUEST_CODES"
    }

    interface Client {

        /**
         * [ServiceBindingAdministrator] which handles a type that implements
         * [BoundService] & [Client]
         */
        class BindingAdministrator<T>(
            context: Context,
            serviceClass: Class<T>
        ) : ServiceBindingAdministrator<T>(context, serviceClass)
                where T : BoundService,
                      T : Client

        val notificationGroup: NotificationGroup
        val requestCodes: PendingIntentRequestCodes

        fun Intent.putCleanupExtras(
            notificationId: Int,
            associatedRequestCodes: ArrayList<Int>,
            cancelNotification: Boolean = false
        ): Intent =
            this
                .putExtra(EXTRA_ASSOCIATED_NOTIFICATION_ID, notificationId)
                .putIntegerArrayListExtra(EXTRA_ASSOCIATED_PENDING_REQUEST_CODES, associatedRequestCodes)
                .putExtra(EXTRA_CANCEL_NOTIFICATION, cancelNotification)

        fun doCleanup(intent: Intent) {
            notificationGroup.onChildNotificationCancelled(intent.getInt(EXTRA_ASSOCIATED_NOTIFICATION_ID))
            requestCodes.removeAll(
                intent.getIntegerArrayListExtra(EXTRA_ASSOCIATED_PENDING_REQUEST_CODES)!!
                    .toSet()
            )
            onCleanupFinishedListener(intent)
        }

        /**
         * To be overriden for carrying out custom clean up actions
         */
        fun onCleanupFinishedListener(intent: Intent) {}
    }

    private val bindingAdministrator = Client.BindingAdministrator(this, ScreenshotListener::class.java)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificationId = intent!!.getInt(EXTRA_ASSOCIATED_NOTIFICATION_ID)

        if (intent.getBooleanExtra(EXTRA_CANCEL_NOTIFICATION, false))
            notificationManager()
                .cancel(notificationId)
                .also { i { "Cancelled notification $notificationId" } }

        bindingAdministrator
            .callOnBoundService {
                it.doCleanup(intent)
            }

        return START_REDELIVER_INTENT
    }

    override fun onDestroy() {
        super.onDestroy()

        bindingAdministrator.unbindService()
    }
}
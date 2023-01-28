package com.w2sv.autocrop.screenshotlistening

import android.content.Context
import android.content.Intent
import com.w2sv.androidutils.extensions.notificationManager
import com.w2sv.autocrop.screenshotlistening.notifications.NotificationGroup
import com.w2sv.autocrop.screenshotlistening.services.ServiceBindingHandler
import com.w2sv.autocrop.screenshotlistening.services.abstrct.BoundService
import com.w2sv.autocrop.screenshotlistening.services.abstrct.UnboundService
import com.w2sv.autocrop.utils.extensions.getInt
import slimber.log.i

abstract class PendingIntentAssociatedResourcesCleanupService<T>(private val clientClass: Class<T>) : UnboundService()
        where T : BoundService,
              T : PendingIntentAssociatedResourcesCleanupService.Client {

    companion object {
        const val EXTRA_ASSOCIATED_NOTIFICATION_ID = "com.w2sv.autocrop.extra.ASSOCIATED_NOTIFICATION_ID"
        const val EXTRA_CANCEL_NOTIFICATION = "com.w2sv.autocrop.extra.CANCEL_NOTIFICATION"
        const val EXTRA_ASSOCIATED_PENDING_REQUEST_CODES = "com.w2sv.autocrop.extra.ASSOCIATED_PENDING_REQUEST_CODES"
    }

    interface Client {

        /**
         * [ServiceBindingHandler] which manages a [BoundService] that also implements [Client]
         */
        class BindingHandler<T>(
            context: Context,
            serviceClass: Class<T>
        ) : ServiceBindingHandler<T>(context, serviceClass)
                where T : BoundService,
                      T : Client

        val notificationGroup: NotificationGroup

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
            notificationGroup.onNotificationCancelled(
                intent.getInt(EXTRA_ASSOCIATED_NOTIFICATION_ID),
                intent.getIntegerArrayListExtra(EXTRA_ASSOCIATED_PENDING_REQUEST_CODES)!!
            )
            onCleanupFinishedListener(intent)
        }

        /**
         * For carrying out custom clean-up actions
         */
        fun onCleanupFinishedListener(intent: Intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent!!.getBooleanExtra(EXTRA_CANCEL_NOTIFICATION, false)) {
            val notificationId = intent.getInt(EXTRA_ASSOCIATED_NOTIFICATION_ID)

            notificationManager()
                .cancel(notificationId)
                .also { i { "Cancelled notification $notificationId" } }
        }

        Client.BindingHandler(this, clientClass)
            .apply {
                callOnBoundService {
                    it.doCleanup(intent)

                    unbindService()
                    stopSelf()
                }
            }

        return START_REDELIVER_INTENT
    }
}
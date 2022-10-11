package com.autocrop.screencapturelistening

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.autocrop.screencapturelistening.notification.ASSOCIATED_NOTIFICATION_ID
import com.autocrop.screencapturelistening.notification.CANCEL_NOTIFICATION_ACTION
import com.autocrop.screencapturelistening.serviceextensions.BoundService
import com.autocrop.screencapturelistening.serviceextensions.UnboundService
import com.autocrop.utils.android.extensions.notificationManager
import com.autocrop.utils.kotlin.delegates.Consumable
import timber.log.Timber

class NotificationCancellationService: UnboundService(){
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.i("NotificationCancellationService.onStartCommand; startId: $startId")
        PendingIntentRequestCode.notificationCancellationService.remove(startId)

        val notificationId = intent!!.getIntExtra(ASSOCIATED_NOTIFICATION_ID, -1)
        intent.action?.run {
            if (equals(CANCEL_NOTIFICATION_ACTION))
                notificationManager().cancel(notificationId)
        }

        if (screenCaptureListeningService == null)
            bindService(
                Intent(this, ScreenCaptureListeningService::class.java),
                serviceConnection.apply {
                    impendingRemoveId = notificationId
                },
                Context.BIND_AUTO_CREATE
            )
        else
            screenCaptureListeningService!!.notificationGroup.onChildNotificationCancelled(notificationId)

        return START_REDELIVER_INTENT
    }

    private var screenCaptureListeningService: ScreenCaptureListeningService? = null

    private val serviceConnection = object: ServiceConnection{
        var impendingRemoveId by Consumable<Int>(null)

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Timber.i("$name connected")
            screenCaptureListeningService = (service as? BoundService.LocalBinder)!!
                .getService<ScreenCaptureListeningService>()
                .apply {
                    impendingRemoveId?.let {
                        notificationGroup.onChildNotificationCancelled(it)
                    }
                }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            screenCaptureListeningService = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        unbindService(serviceConnection)
    }
}
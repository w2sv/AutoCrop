package com.autocrop.screencapturelistening

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.autocrop.utils.android.extensions.notificationManager
import com.autocrop.utils.kotlin.delegates.Consumable
import timber.log.Timber

class NotificationCancellationService: UnboundService(){
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.i("NotificationCancellationService.onStartCommand")

        val notificationId = intent!!.getIntExtra(NOTIFICATION_ID_EXTRA_KEY, -1)
        if (intent.hasExtra(CANCEL_NOTIFICATION_EXTRA_KEY)){
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
            screenCaptureListeningService!!.removeNotification(notificationId)

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
                        removeNotification(it)
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
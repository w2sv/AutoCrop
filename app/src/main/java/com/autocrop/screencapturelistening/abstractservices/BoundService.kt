package com.autocrop.screencapturelistening.abstractservices

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.autocrop.screencapturelistening.notification.NotificationGroup
import com.autocrop.screencapturelistening.notification.ScopeWideUniqueIds

abstract class BoundService(uniqueServiceNumber: Int): LoggingService(){

    inner class LocalBinder : Binder() {
        @Suppress("UNCHECKED_CAST")
        fun <T: BoundService> getService(): T =
            (this@BoundService) as T
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder = binder

    companion object{
        const val CANCELLATION_CLIENT = "CANCELLATION_CLIENT"
    }
    protected val cancellationClientName: String = this::class.java.name
    abstract val notificationGroup: NotificationGroup
    val cancellationRequestCodes = ScopeWideUniqueIds(uniqueServiceNumber * 100)
    open fun onCancellation(intent: Intent){}
}
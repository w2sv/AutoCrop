package com.autocrop.screencapturelistening

import android.content.Intent
import com.autocrop.screencapturelistening.abstractservices.UnboundService
import com.autocrop.screencapturelistening.notification.ASSOCIATED_NOTIFICATION_ID
import com.autocrop.screencapturelistening.notification.ASSOCIATED_PENDING_REQUEST_CODES
import com.autocrop.utils.android.extensions.getInt
import com.autocrop.utils.android.extensions.getParcelable
import com.autocrop.utils.android.extensions.notificationManager

class CropIOServiceActionIntentInterceptor: UnboundService(){
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        with(intent!!){
            val notificationId = getInt(ASSOCIATED_NOTIFICATION_ID)
            notificationManager().cancel(notificationId)
            bindingAdministrator.callOnBoundService {
                it.notificationGroup.onChildNotificationCancelled(notificationId)
                it.pendingRequestCodes.removeAll(
                    getIntArrayExtra(ASSOCIATED_PENDING_REQUEST_CODES)!!
                        .toSet()
                )
            }

            startActivity(
                getParcelable<Intent>(CropIOService.WRAPPED_INTENT)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private val bindingAdministrator = BindingAdministrator(this, CropIOService::class.java)

    override fun onDestroy() {
        super.onDestroy()

        bindingAdministrator.unbindService()
    }
}
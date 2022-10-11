package com.autocrop.screencapturelistening

import com.autocrop.screencapturelistening.notification.ScopeWideUniqueIds

object PendingIntentRequestCodes{
    val cropIOService = ScopeWideUniqueIds()
    val notificationCancellationService = ScopeWideUniqueIds()
    val viewCrop = ScopeWideUniqueIds()
    val shareCrop = ScopeWideUniqueIds()

    fun clear(){
        listOf(cropIOService, notificationCancellationService, viewCrop, shareCrop)
            .forEach {
                it.clear()
            }
    }
}
package com.autocrop.screencapturelistening.notification

import com.autocrop.utils.kotlin.extensions.nonZeroOrdinal

enum class NotificationId{
    STARTED_FOREGROUND_SERVICE,
    DETECTED_NEW_CROPPABLE_SCREENSHOT,
    SUCCESSFULLY_SAVED_CROP;

    val id: Int get() = nonZeroOrdinal
    val channelId: String get() = name
}
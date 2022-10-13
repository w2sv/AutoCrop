package com.autocrop.screencapturelistening.notifications

import com.autocrop.utils.kotlin.extensions.nonZeroOrdinal

enum class NotificationId{
    STARTED_FOREGROUND_SERVICE,
    DETECTED_NEW_CROPPABLE_SCREENSHOT,
    SAVED_CROP;

    val id: Int get() = nonZeroOrdinal
    val channelId: String get() = name
}
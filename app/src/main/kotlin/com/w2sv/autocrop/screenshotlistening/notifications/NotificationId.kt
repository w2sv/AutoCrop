package com.w2sv.autocrop.screenshotlistening.notifications

import com.w2sv.kotlinutils.extensions.nonZeroOrdinal

enum class NotificationId {
    STARTED_FOREGROUND_SERVICE,
    DETECTED_NEW_CROPPABLE_SCREENSHOT;

    val id: Int by ::nonZeroOrdinal
    val channelId: String by ::name
}
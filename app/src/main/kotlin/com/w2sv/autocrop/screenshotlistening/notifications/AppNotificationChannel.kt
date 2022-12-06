package com.w2sv.autocrop.screenshotlistening.notifications

import com.w2sv.kotlinutils.extensions.nonZeroOrdinal

enum class AppNotificationChannel(val title: String) {
    STARTED_FOREGROUND_SERVICE("Listening to screen captures"),
    DETECTED_NEW_CROPPABLE_SCREENSHOT("Detected croppable screenshots");

    val id: String by ::name
    val requestCodeSeed: Int by ::ordinal
    val childIdSeed: Int by ::nonZeroOrdinal
    val groupSummaryId: Int by ::childIdSeed
}
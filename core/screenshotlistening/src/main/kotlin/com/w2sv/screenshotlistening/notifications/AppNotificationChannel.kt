package com.w2sv.screenshotlistening.notifications

enum class AppNotificationChannel(val title: String) {
    STARTED_FOREGROUND_SERVICE("Listening to screen captures"),
    DETECTED_NEW_CROPPABLE_SCREENSHOT("Detected croppable screenshots");

    val id: String by ::name
    val requestCodeSeed: Int by ::ordinal
    val childIdSeed: Int get() = ordinal + 1
    val groupSummaryId: Int by ::childIdSeed
}
package com.autocrop.screencapturelistening

enum class NotificationId{
    STARTED_FOREGROUND_SERVICE,
    DETECTED_NEW_CROPPABLE_SCREENSHOT,
    SUCCESSFULLY_SAVED_CROP;

    val groupKey: String get() = "${name}_GROUP"
}
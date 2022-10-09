package com.autocrop.screencapturelistening

import com.autocrop.utils.kotlin.extensions.nonZeroOrdinal
import java.util.PriorityQueue

enum class NotificationId{
    STARTED_FOREGROUND_SERVICE,
    DETECTED_NEW_CROPPABLE_SCREENSHOT,
    SUCCESSFULLY_SAVED_CROP;

    val groupKey: String get() = "${name}_GROUP"
}

class UsedNotificationIds(groupId: NotificationId): PriorityQueue<Int>(){
    val channelId: String = groupId.name
    private val idBase: Int = groupId.nonZeroOrdinal * 100

    fun addId(): Int =
        (idBase + (lastOrNull()?.let { it + 1 } ?: 0)).also {
            add(it)
        }
}
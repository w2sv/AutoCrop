package com.autocrop.screencapturelistening.notifications

import androidx.core.app.NotificationCompat
import com.autocrop.utils.kotlin.extensions.nonZeroOrdinal
import java.util.PriorityQueue

class UniqueNotificationIdsWithBuilder(groupId: NotificationId) : PriorityQueue<Pair<Int, NotificationCompat.Builder>>(
    compareBy { it.first }) {
    private val idBase: Int = groupId.nonZeroOrdinal * 100

    fun newId(): Int =
        lastOrNull()?.let { it.first + 1 }
            ?: idBase

    fun remove(id: Int): Boolean =
        removeIf { it.first == id }
}
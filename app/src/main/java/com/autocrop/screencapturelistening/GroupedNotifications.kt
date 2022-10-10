package com.autocrop.screencapturelistening

import androidx.core.app.NotificationCompat
import com.autocrop.utils.kotlin.extensions.nonZeroOrdinal
import java.util.PriorityQueue

class GroupedNotifications(groupId: NotificationId)
    : PriorityQueue<Pair<Int, NotificationCompat.Builder>>(compareBy { it.first }){
    val channelId: String = groupId.name
    private val idBase: Int = groupId.nonZeroOrdinal * 100

    fun newId(): Int =
        lastOrNull()?.let { it.first + 1 } ?: idBase

    fun remove(id: Int): Boolean =
        removeIf { it.first == id }
}
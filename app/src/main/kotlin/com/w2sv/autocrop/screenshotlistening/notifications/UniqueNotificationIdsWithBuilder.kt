package com.w2sv.autocrop.screenshotlistening.notifications

import androidx.core.app.NotificationCompat
import com.w2sv.kotlinutils.extensions.nonZeroOrdinal
import java.util.PriorityQueue

class UniqueNotificationIdsWithBuilder(groupId: NotificationId) : PriorityQueue<Pair<Int, NotificationCompat.Builder>>(
    compareBy { it.first }) {

    private val idBase: Int = PendingIntentRequestCodes.uniqueIdBase(groupId.nonZeroOrdinal)

    fun newId(): Int =
        lastOrNull()?.let { it.first + 1 }
            ?: idBase

    fun remove(id: Int): Boolean =
        removeIf { it.first == id }
}
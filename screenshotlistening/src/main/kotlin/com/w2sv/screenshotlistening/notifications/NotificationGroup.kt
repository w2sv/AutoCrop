package com.w2sv.screenshotlistening.notifications

import android.content.Context
import android.content.ContextWrapper
import androidx.core.app.NotificationCompat
import com.w2sv.androidutils.notifying.getNotificationManager
import com.w2sv.androidutils.notifying.showNotification
import slimber.log.i

class NotificationGroup(
    context: Context,
    private val notificationChannel: AppNotificationChannel,
    private val summaryBuilderConfigurator: NotificationCompat.Builder.(nChildren: Int) -> NotificationCompat.Builder
) : ContextWrapper(context) {

    val childrenIds = UniqueGroupedIds(baseSeed = notificationChannel.childIdSeed)
    val requestCodes = UniqueGroupedIds(baseSeed = notificationChannel.requestCodeSeed)
    private val groupKey = "GROUP_${notificationChannel.id}"

    fun addChild(id: Int, builderConfigurator: NotificationCompat.Builder.() -> NotificationCompat.Builder) {
        if (childrenIds.isNotEmpty())
            showSummaryNotification()

        childrenIds.add(id)
            .also { i { "Added ${notificationChannel.name} notification $id" } }

        showNotification(
            id,
            setChannelAndGetNotificationBuilder(
                notificationChannel
            )
                .setOnlyAlertOnce(true)
                .setGroup(groupKey)
                .builderConfigurator()
        )
    }

    private fun showSummaryNotification() {
        showNotification(
            notificationChannel.groupSummaryId,
            setChannelAndGetNotificationBuilder(
                notificationChannel,
            )
                .summaryBuilderConfigurator(childrenIds.size)
                .setGroup(groupKey)
                .setGroupSummary(true)
        )
    }

    fun onNotificationCancelled(id: Int, associatedRequestCodes: Collection<Int>) {
        childrenIds.remove(id)
        i { "Removed notification id $id; New # notifications: ${childrenIds.size}" }
        requestCodes.removeAll(associatedRequestCodes.toSet())
        i { "Removed request codes $associatedRequestCodes" }

        if (childrenIds.isEmpty())
            getNotificationManager().cancel(notificationChannel.childIdSeed)
    }
}
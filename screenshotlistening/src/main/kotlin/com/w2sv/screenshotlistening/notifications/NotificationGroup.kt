package com.w2sv.screenshotlistening.notifications

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import slimber.log.i

class NotificationGroup(
    private val notificationManager: NotificationManager,
    private val context: Context,
    private val notificationChannel: AppNotificationChannel,
    private val summaryBuilderConfigurator: NotificationCompat.Builder.(nChildren: Int) -> NotificationCompat.Builder
) {
    val childrenIds = UniqueGroupedIds(baseSeed = notificationChannel.childIdSeed)
    val requestCodes = UniqueGroupedIds(baseSeed = notificationChannel.requestCodeSeed)
    private val groupKey = "GROUP_${notificationChannel.id}"

    fun addChild(id: Int, builderConfigurator: NotificationCompat.Builder.() -> NotificationCompat.Builder) {
        if (childrenIds.isNotEmpty())
            showSummaryNotification()

        childrenIds.add(id)
        i { "Added ${notificationChannel.name} notification $id" }

        notificationManager.notify(
            id,
            context.setChannelAndGetNotificationBuilder(
                notificationManager,
                notificationChannel
            )
                .setOnlyAlertOnce(true)
                .setGroup(groupKey)
                .builderConfigurator()
                .build()
        )
    }

    private fun showSummaryNotification() {
        notificationManager.notify(
            notificationChannel.groupSummaryId,
            context.setChannelAndGetNotificationBuilder(
                notificationManager,
                notificationChannel,
            )
                .summaryBuilderConfigurator(childrenIds.size)
                .setGroup(groupKey)
                .setGroupSummary(true)
                .build()
        )
    }

    fun onNotificationCancelled(id: Int, associatedRequestCodes: Collection<Int>) {
        childrenIds.remove(id)
        i { "Removed notification id $id; New # notifications: ${childrenIds.size}" }
        requestCodes.removeAll(associatedRequestCodes.toSet())
        i { "Removed request codes $associatedRequestCodes" }

        if (childrenIds.isEmpty()) {
            notificationManager.cancel(notificationChannel.childIdSeed)
        }
    }
}
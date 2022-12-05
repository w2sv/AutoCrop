package com.w2sv.autocrop.screenshotlistening.notifications

import android.content.Context
import android.content.ContextWrapper
import androidx.core.app.NotificationCompat
import com.w2sv.androidutils.extensions.notificationManager
import com.w2sv.androidutils.extensions.showNotification
import slimber.log.i

class NotificationGroup(
    context: Context,
    private val channelName: String,
    private val summaryId: NotificationId,
    private val makeSummaryTitle: (Int) -> String,
    private val applyToSummaryBuilder: (NotificationCompat.Builder.() -> NotificationCompat.Builder)? = null
) : ContextWrapper(context) {

    val children = UniqueNotificationIdsWithBuilder(summaryId)
    private val groupKey = "GROUP_${summaryId.name}"
    private val channelId: String by summaryId::channelId

    fun childBuilder(title: String): NotificationCompat.Builder =
        setChannelAndGetNotificationBuilder(
            channelId,
            title,
            channelName
        )
            .setOnlyAlertOnce(true)
            .setGroup(groupKey)

    fun addChild(id: Int, builder: NotificationCompat.Builder) {
        with(children) {
            if (size >= 1)
                showSummaryNotification()
            if (size == 1)
                element().let {
                    showNotification(
                        it.first,
                        it.second
                            .setSilent(true)
                            .setGroup(groupKey)
                    )
                }

            add(id to builder)
                .also { i { "Added ${summaryId.name} notification $id" } }
        }

        showNotification(id, builder)
    }

    fun onChildNotificationCancelled(id: Int) {
        with(children) {
            remove(id)
                .also { i { "Removed notification id $id; New # notifications: $size" } }

            if (size == 1)
                element().let {
                    showNotification(
                        it.first,
                        it.second
                            .setSilent(true)
                            .setGroup(null)
                    )
                }
            if (size <= 1)
                notificationManager().cancel(summaryId.id)
        }
    }

    private fun showSummaryNotification() {
        showNotification(
            summaryId.id,
            setChannelAndGetNotificationBuilder(
                channelId,
                makeSummaryTitle(children.size),
                channelName
            )
                .apply {
                    applyToSummaryBuilder?.invoke(this)
                }
                .setGroup(groupKey)
                .setGroupSummary(true)
        )
    }
}
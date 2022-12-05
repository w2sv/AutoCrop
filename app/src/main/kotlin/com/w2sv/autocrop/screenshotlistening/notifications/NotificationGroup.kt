package com.w2sv.autocrop.screenshotlistening.notifications

import android.content.Context
import android.content.ContextWrapper
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import com.w2sv.androidutils.extensions.notificationManager
import com.w2sv.androidutils.extensions.showNotification
import slimber.log.i

class NotificationGroup(
    context: Context,
    private val channelName: String,
    private val summaryId: NotificationId,
    @StringRes private val summaryTextStringResource: Int,
    private val applyToSummaryBuilder: NotificationCompat.Builder.() -> NotificationCompat.Builder = { this }
) : ContextWrapper(context) {

    val children = UniqueNotificationIdsWithBuilder(summaryId)
    private val groupKey = "GROUP_${summaryId.name}"
    private val channelId: String by summaryId::channelId

    fun getChildBuilder(title: String): NotificationCompat.Builder =
        setChannelAndGetNotificationBuilder(
            channelId,
            title,
            channelName
        )
            .setOnlyAlertOnce(true)
            .setGroup(groupKey)

    fun addChild(id: Int, builder: NotificationCompat.Builder) {
        if (children.size >= 1)
            showSummaryNotification()

        if (children.size == 1)
            children.element().let {
                showNotification(
                    it.first,
                    it.second
                        .setSilent(true)
                        .setGroup(groupKey)
                )
            }

        children.add(id to builder)
            .also { i { "Added ${summaryId.name} notification $id" } }

        showNotification(id, builder)
    }

    private fun showSummaryNotification() {
        showNotification(
            summaryId.id,
            setChannelAndGetNotificationBuilder(
                channelId,
                getString(summaryTextStringResource, children.size),
                channelName
            )
                .applyToSummaryBuilder()
                .setGroup(groupKey)
                .setGroupSummary(true)
        )
    }

    fun onChildNotificationCancelled(id: Int) {
        children.remove(id)
            .also { i { "Removed notification id $id; New # notifications: ${children.size}" } }

        if (children.size == 1)
            children.element().let {
                showNotification(
                    it.first,
                    it.second
                        .setSilent(true)
                        .setGroup(null)
                )
            }

        if (children.size <= 1)
            notificationManager().cancel(summaryId.id)
    }
}
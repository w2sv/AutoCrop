package com.w2sv.autocrop.screenshotlistening.notifications

import android.content.Context
import android.content.ContextWrapper
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import com.w2sv.androidutils.extensions.notificationManager
import com.w2sv.androidutils.extensions.showNotification
import com.w2sv.kotlinutils.extensions.nonZeroOrdinal
import slimber.log.i

private typealias ApplyToBuilder = NotificationCompat.Builder.() -> NotificationCompat.Builder

class NotificationGroup(
    context: Context,
    private val channelName: String,
    pendingIntentRequestCodesSeed: Int,
    private val childTitle: String,
    private val summaryId: NotificationId,
    @StringRes private val summaryTextStringResource: Int,
    private val applyToSummaryBuilder: ApplyToBuilder = { this }
) : ContextWrapper(context) {

    val children = UniqueAssociatedIds(baseSeed = summaryId.nonZeroOrdinal)
    val requestCodes = PendingIntentRequestCodes(pendingIntentRequestCodesSeed)
    private val groupKey = "GROUP_${summaryId.name}"
    private val channelId: String by summaryId::channelId

    fun addChild(id: Int, applyToBuilder: ApplyToBuilder) {
        if (children.isNotEmpty())
            showSummaryNotification()

        children.add(id)
            .also { i { "Added ${summaryId.name} notification $id" } }

        showNotification(
            id,
            getChildBuilder()
                .applyToBuilder()
        )
    }

    private fun getChildBuilder(): NotificationCompat.Builder =
        setChannelAndGetNotificationBuilder(
            channelId,
            childTitle,
            channelName
        )
            .setOnlyAlertOnce(true)
            .setGroup(groupKey)

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

    fun onChildNotificationCancelled(id: Int, associatedRequestCodes: Collection<Int>) {
        children.remove(id)
        requestCodes.removeAll(associatedRequestCodes.toSet())
        i { "Removed notification id $id; New # notifications: ${children.size}" }

        if (children.isEmpty())
            notificationManager().cancel(summaryId.id)
    }
}
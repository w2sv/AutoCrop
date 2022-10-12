package com.autocrop.screencapturelistening.notifications

import android.content.Context
import android.content.ContextWrapper
import androidx.core.app.NotificationCompat
import com.autocrop.utils.android.extensions.notificationBuilderWithSetChannel
import com.autocrop.utils.android.extensions.notificationManager
import com.autocrop.utils.android.extensions.showNotification
import timber.log.Timber

class NotificationGroup(context: Context,
                        private val summaryId: NotificationId,
                        private val makeSummaryTitle: (Int) -> String,
                        private val applyToSummaryBuilder: ((NotificationCompat.Builder) -> NotificationCompat.Builder)? = null
) : ContextWrapper(context) {
    val children = UniqueNotificationIdsWithBuilder(summaryId)
    private val channelId: String get() = summaryId.channelId
    private val groupKey = "GROUP_${summaryId.name}"

    fun childBuilder(title: String, text: String? = null): NotificationCompat.Builder =
        notificationBuilderWithSetChannel(
            channelId,
            title,
            text
        )
            .setOnlyAlertOnce(true)
            .setGroup(groupKey)

    fun addAndShowChild(id: Int, builder: NotificationCompat.Builder){
        showSummaryNotificationIfApplicable()

        children.run {
            if (size == 1)
                with(element()){
                    showNotification(first, second.setSilent(true))
                }
        }
        children.add(id to builder)
        Timber.i("Added ${summaryId.name} notification $id")

        showNotification(id, builder)
    }

    fun onChildNotificationCancelled(id: Int) {
        children.remove(id)
        Timber.i("Removed notification id $id; Child notifications size: ${children.size}")
        cancelSummaryNotificationIfApplicable()
    }

    private fun showSummaryNotificationIfApplicable() {
        if (children.size >= 1) {
            showNotification(
                summaryId.id,
                notificationBuilderWithSetChannel(
                    channelId,
                    makeSummaryTitle(children.size)
                )
                    .apply {
                        applyToSummaryBuilder?.invoke(this)
                    }
                    .setGroup(groupKey)
                    .setGroupSummary(true)
            )
        }
    }

    private fun cancelSummaryNotificationIfApplicable() {
        if (children.isEmpty())
            notificationManager().cancel(summaryId.id)
    }
}
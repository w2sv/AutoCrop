package com.autocrop.screencapturelistening.notification

import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.autocrop.screencapturelistening.NotificationCancellationService
import com.autocrop.screencapturelistening.PendingIntentRequestCode
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

    fun childNotificationDeleteIntent(id: Int): PendingIntent =
        PendingIntent.getService(
            this,
            PendingIntentRequestCode.notificationCancellationService.addNewId(),
            Intent(
                this,
                NotificationCancellationService::class.java
            )
                .putExtra(ASSOCIATED_NOTIFICATION_ID, id),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
        )

    fun addAndShowChild(id: Int, builder: NotificationCompat.Builder){
        children.add(id to builder)
        Timber.i("Added ${summaryId.name} child $id")
        showNotification(id, builder)
        showSummaryNotificationIfApplicable()
    }

    fun onChildNotificationCancelled(id: Int) {
        children.remove(id)
        Timber.i("Removed child notification id $id; New number of child notifications: ${children.size}")
        cancelSummaryNotificationIfApplicable()
    }

    private fun showSummaryNotificationIfApplicable() {
        if (children.size >= 2) {
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
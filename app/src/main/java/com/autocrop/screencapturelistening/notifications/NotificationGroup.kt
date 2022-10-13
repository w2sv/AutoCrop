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

    fun addChild(id: Int, builder: NotificationCompat.Builder){
        with(children){
            if (size >= 1)
                showSummaryNotification()
            if (size == 1)
                with(element()){
                    showNotification(
                        first,
                        second
                            .setSilent(true)
                            .setGroup(groupKey)
                    )
                }

            add(id to builder)
                .also { Timber.i("Added ${summaryId.name} notification $id") }
        }

        showNotification(id, builder)
    }

    fun onChildNotificationCancelled(id: Int) {
        with(children){
            remove(id)
                .also { Timber.i("Removed notification id $id; n notifications: $size") }

            if (size == 1)
                with(element()){
                    showNotification(
                        first,
                        second
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
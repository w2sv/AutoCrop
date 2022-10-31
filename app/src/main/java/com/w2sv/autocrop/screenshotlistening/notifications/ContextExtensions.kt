package com.w2sv.autocrop.screenshotlistening.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.w2sv.autocrop.R

fun Context.showNotification(id: Int, builder: NotificationCompat.Builder) {
    notificationManager()
        .notify(
            id,
            builder.build()
        )
}

fun Context.notificationBuilderWithSetChannel(
    channelId: String,
    title: String,
    channelName: String? = null
): NotificationCompat.Builder {
    notificationManager().createNotificationChannel(
        NotificationChannel(
            channelId,
            channelName
                ?: title,
            NotificationManager.IMPORTANCE_DEFAULT
        )
    )
    return notificationBuilder(channelId, title)
}

private fun Context.notificationBuilder(
    channelId: String,
    title: String,
): NotificationCompat.Builder =
    NotificationCompat.Builder(this, channelId)
        .setSmallIcon(R.drawable.ic_scissors_24)
        .setContentTitle(title)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

fun Context.notificationManager(): NotificationManager =
    (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
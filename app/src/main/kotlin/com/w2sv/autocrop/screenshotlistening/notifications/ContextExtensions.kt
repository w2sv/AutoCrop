package com.w2sv.autocrop.screenshotlistening.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.w2sv.androidutils.extensions.getNotificationManager
import com.w2sv.autocrop.R

fun Context.setChannelAndGetNotificationBuilder(
    channel: AppNotificationChannel,
    contentTitle: String? = null
): NotificationCompat.Builder {
    getNotificationManager().createNotificationChannel(
        NotificationChannel(
            channel.id,
            channel.title,
            NotificationManager.IMPORTANCE_DEFAULT
        )
    )
    return notificationBuilder(channel.id, contentTitle)
}

private fun Context.notificationBuilder(
    channelId: String,
    title: String?,
): NotificationCompat.Builder =
    NotificationCompat.Builder(this, channelId)
        .setSmallIcon(R.drawable.ic_scissors_24)
        .setContentTitle(title)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
package com.autocrop.utils.android.extensions

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Binder
import android.os.Build
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.autocrop.screencapturelistening.NotificationId
import com.w2sv.autocrop.R

fun Context.getColoredIcon(@DrawableRes drawableId: Int, @ColorRes colorId: Int): Drawable =
    DrawableCompat.wrap(AppCompatResources.getDrawable(this, drawableId)!!).apply {
        setColor(this@getColoredIcon, colorId)
    }

fun Context.getThemedColor(@ColorRes id: Int): Int =
    resources.getColor(id, theme)

/**
 * [Context.getSharedPreferences] with key=[Context.getPackageName] and [Context.MODE_PRIVATE]
 */
fun Context.getApplicationWideSharedPreferences(): SharedPreferences =
    getSharedPreferences(packageName, Context.MODE_PRIVATE)

fun Context.uriPermissionGranted(uri: Uri, permissionCode: Int): Boolean =
    checkUriPermission(
        uri,
        null,
        null,
        Binder.getCallingPid(),
        Binder.getCallingUid(),
        permissionCode
    ) == PackageManager.PERMISSION_GRANTED

tailrec fun Context.getActivity(): Activity? =
    this as? Activity ?: (this as? ContextWrapper)?.baseContext?.getActivity()

fun Context.showNotification(channelId: String,
                             channelName: String,
                             title: String,
                             text: String,
                             notificationId: NotificationId,
                             action: NotificationCompat.Action? = null) {
    val notificationBuilder: NotificationCompat.Builder =
        NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_scissors_24)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .apply {
                action?.let {
                    addAction(it)
                }
            }

    val notificationManager = notificationManager().apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createNotificationChannel(
                NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
    }

    notificationManager.notify(notificationId.ordinal, notificationBuilder.build())
}

fun Context.cancelNotification(id: NotificationId){
    notificationManager().cancel(id.ordinal)
}

fun Context.notificationManager(): NotificationManager =
    (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
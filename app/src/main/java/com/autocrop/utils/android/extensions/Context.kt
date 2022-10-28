package com.autocrop.utils.android.extensions

import android.app.Activity
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Binder
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.DrawableCompat
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
    this as? Activity
        ?: (this as? ContextWrapper)?.baseContext?.getActivity()

@Suppress("DEPRECATION")
inline fun <reified T : Service> Context.serviceRunning() =
    (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
        .getRunningServices(Integer.MAX_VALUE)
        .any { it.service.className == T::class.java.name }

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
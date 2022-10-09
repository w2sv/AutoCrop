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
import com.autocrop.screencapturelistening.NotificationId
import com.autocrop.utils.kotlin.extensions.nonZeroOrdinal
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

@Suppress("DEPRECATION")
inline fun <reified T: Service> Context.serviceRunning() =
    (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
        .getRunningServices(Integer.MAX_VALUE)
        .any { it.service.className == T::class.java.name }

fun Context.showNotification(id: NotificationId,
                             title: String,
                             text: String,
                             action: NotificationCompat.Action? = null) {
    notificationManager().apply {
        createNotificationChannel(id, title)
    }
        .notify(
            id.nonZeroOrdinal,
            notificationBuilder(id, title, text, action)
                .build()
        )
}

fun Context.showNotification(id: NotificationId, builder: NotificationCompat.Builder){
    notificationManager()
        .notify(
            id.nonZeroOrdinal,
            builder
                .build()
        )
}

fun Context.notificationBuilderWithSetChannel(id: NotificationId,
                                              title: String,
                                              text: String? = null,
                                              action: NotificationCompat.Action? = null): NotificationCompat.Builder{
    notificationManager().createNotificationChannel(id, title)
    return notificationBuilder(id, title, text, action)
}

private fun NotificationManager.createNotificationChannel(id: NotificationId,
                                                  channelName: String){
    createNotificationChannel(
        NotificationChannel(
            id.name,
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT
        )
    )
}

private fun Context.notificationBuilder(id: NotificationId,
                                title: String,
                                text: String?,
                                action: NotificationCompat.Action? = null): NotificationCompat.Builder =
    NotificationCompat.Builder(this, id.name)
        .setSmallIcon(R.drawable.ic_scissors_24)
        .setContentTitle(title)
        .setContentText(text)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .apply {
            action?.let {
                addAction(it)
            }
        }

fun Context.cancelNotification(id: NotificationId){
    notificationManager().cancel(id.nonZeroOrdinal)
}

fun Context.notificationManager(): NotificationManager =
    (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
package com.autocrop.utils.android

import android.app.Activity
import android.content.pm.PackageManager

fun Activity.permissionGranted(permission: String): Boolean =
    checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
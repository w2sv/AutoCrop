package com.w2sv.permissionhandler

import com.w2sv.kotlinutils.UnitFun

fun Iterator<PermissionHandler?>.requestPermissions(onGranted: UnitFun, onDenied: UnitFun? = null) {
    if (!hasNext())
        onGranted()
    else {
        next()?.requestPermission(
            onGranted = { requestPermissions(onGranted, onDenied) },
            onDenied = onDenied
        )
            ?: requestPermissions(onGranted, onDenied)
    }
}
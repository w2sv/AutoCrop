package com.w2sv.permissionhandler

fun Iterator<PermissionHandler?>.requestPermissions(onGranted: () -> Unit, onDenied: (() -> Unit)? = null) {
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
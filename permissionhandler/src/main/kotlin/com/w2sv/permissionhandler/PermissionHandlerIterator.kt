package com.w2sv.permissionhandler

fun Iterable<PermissionHandler>.requestPermissions(onGranted: () -> Unit, onDenied: (() -> Unit)? = null) {
    iterator().requestPermissions(onGranted, onDenied)
}

private fun Iterator<PermissionHandler>.requestPermissions(onGranted: () -> Unit, onDenied: (() -> Unit)? = null) {
    if (!hasNext())
        onGranted()
    else {
        next().requestPermission(
            onGranted = { requestPermissions(onGranted, onDenied) },
            onDenied = onDenied
        )
    }
}
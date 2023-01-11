package com.w2sv.permissionhandler

fun Iterable<PermissionHandler>.requestPermissions(
    onGranted: () -> Unit,
    onDenied: (() -> Unit)? = null,
    onDialogClosed: (() -> Unit)? = null
) {
    iterator().requestPermissions(onGranted, onDenied, onDialogClosed)
}

private fun Iterator<PermissionHandler>.requestPermissions(
    onGranted: () -> Unit,
    onDenied: (() -> Unit)? = null,
    onDialogClosed: (() -> Unit)? = null
) {
    if (!hasNext()) {
        onGranted()
        onDialogClosed?.invoke()
    }
    else {
        next().requestPermission(
            onPermissionGranted = { requestPermissions(onGranted, onDenied, onDialogClosed) },
            onPermissionDenied = onDenied
        )
    }
}
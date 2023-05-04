package com.w2sv.common

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import com.w2sv.androidutils.permissions.permissionhandler.SingularPermissionHandler
import com.w2sv.common.extensions.snackyBuilder
import de.mateware.snacky.Snacky

class PermissionHandler(
    activity: ComponentActivity,
    permission: String,
    private val permissionDeniedMessage: String,
    private val permissionRationalSuppressedMessage: String
) : SingularPermissionHandler(activity, permission, "PermissionHandler") {

    override fun requestPermissionIfRequired(
        onGranted: () -> Unit,
        onDenied: (() -> Unit)?,
        onRequestDismissed: (() -> Unit)?
    ): Boolean {
        return super.requestPermissionIfRequired(
            onGranted,
            {
                getSnackyBuilder(permissionDeniedMessage)
                    .build()
                    .show()
                onDenied?.invoke()
            },
            onRequestDismissed
        )
    }

    override fun onPermissionRationalSuppressed() {
        getSnackyBuilder(permissionRationalSuppressedMessage)
            .setActionText("Settings")
            .setActionClickListener {
                activity.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(
                            Uri.fromParts(
                                "package",
                                activity.packageName,
                                null
                            )
                        )
                )
            }
            .build()
            .show()
    }

    private fun getSnackyBuilder(text: String): Snacky.Builder =
        activity.snackyBuilder(text)
            .setIcon(R.drawable.ic_error_24)
}
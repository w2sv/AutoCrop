package com.w2sv.common

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import com.w2sv.androidutils.permissions.permissionhandler.SingularPermissionHandler
import com.w2sv.common.extensions.getSnackyBuilder
import de.mateware.snacky.Snacky

class PermissionHandler(
    activity: ComponentActivity,
    permission: String,
    @StringRes private val permissionDeniedMessageRes: Int,
    @StringRes private val permissionRationalSuppressedMessageRes: Int
) : SingularPermissionHandler(activity, permission, "PermissionHandler") {

    override fun requestPermissionIfRequired(
        onGranted: () -> Unit,
        onDenied: (() -> Unit)?,
        onRequestDismissed: (() -> Unit)?
    ): Boolean {
        return super.requestPermissionIfRequired(
            onGranted,
            {
                getSnackyBuilder(activity.getString(permissionDeniedMessageRes))
                    .build()
                    .show()
                onDenied?.invoke()
            },
            onRequestDismissed
        )
    }

    override fun onPermissionRationalSuppressed() {
        getSnackyBuilder(activity.getString(permissionRationalSuppressedMessageRes))
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
        activity.getSnackyBuilder(text)
            .setIcon(R.drawable.ic_error_24)
}
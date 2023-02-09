package com.w2sv.autocrop.utils

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import com.w2sv.autocrop.R
import com.w2sv.autocrop.utils.extensions.snackyBuilder
import com.w2sv.permissionhandler.PermissionHandler
import de.mateware.snacky.Snacky

class PermissionHandler(
    activity: ComponentActivity,
    permission: String,
    private val permissionDeniedMessage: String,
    private val permissionRationalSuppressedMessage: String
) : PermissionHandler(activity, permission) {

    override fun onPermissionDenied() {
        getSnackyBuilder(permissionDeniedMessage)
            .build()
            .show()
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
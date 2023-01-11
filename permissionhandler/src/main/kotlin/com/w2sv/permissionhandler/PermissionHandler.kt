@file:Suppress("unused")

package com.w2sv.permissionhandler

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.w2sv.androidutils.ActivityCallContractAdministrator
import de.mateware.snacky.Snacky

class PermissionHandler(
    private val activity: ComponentActivity,
    private val permission: String,
    private val permissionDeniedMessage: String,
    private val permissionRequestingSuppressedMessage: String
) : ActivityCallContractAdministrator.Impl<String, Boolean>(
    activity,
    ActivityResultContracts.RequestPermission()
) {
    companion object {
        /**
         * Permissions declared within package Manifest actually required by the api,
         * that is whose maxSdkVersion (also manually declared in Manifest) <= Build version
         */
        private lateinit var requiredPermissions: Set<String>

        fun setRequiredPermissions(context: Context) {
            requiredPermissions = context.run {
                (
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                            packageManager.getPackageInfo(
                                packageName,
                                PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong())
                            )
                        else
                            @Suppress("DEPRECATION")
                            packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
                        )
                    .requestedPermissions.toSet()
            }
        }
    }

    /**
     * Function wrapper either directly running [onPermissionGranted] if permission granted,
     * otherwise sets [onPermissionGranted] and launches [activityResultCallback]
     */
    fun requestPermission(
        onPermissionGranted: () -> Unit,
        onPermissionDenied: (() -> Unit)? = null,
        onDialogClosed: (() -> Unit)? = null
    ) {
        if (!grantRequired)
            onPermissionGranted()
        else {
            this.onPermissionGranted = onPermissionGranted
            this.onPermissionDenied = onPermissionDenied
            this.onDialogClosed = onDialogClosed

            activityResultLauncher.launch(permission)
        }
    }

    /**
     * Temporary callable to be set before, and to be cleared on exiting of [activityResultCallback]
     */
    private var onPermissionGranted: (() -> Unit)? = null
    private var onPermissionDenied: (() -> Unit)? = null
    private var onDialogClosed: (() -> Unit)? = null

    override val registryKey: String = "${this::class.java.name}.$permission"

    /**
     * Display getSnackyBuilder if some permission hasn't been granted,
     * otherwise run previously set [onPermissionGranted]
     */
    override val activityResultCallback: (Boolean) -> Unit = { permissionGranted ->
        if (!permissionGranted) {
            activity.run {
                if (shouldShowRequestPermissionRationale(permission))
                    permissionDeniedSnacky()
                else
                    permissionRequestingSuppressedSnacky()
            }
                .build()
                .show()

            onPermissionDenied?.invoke()
        }
        else
            onPermissionGranted?.invoke()

        onDialogClosed?.invoke()

        onDialogClosed = null
        onPermissionDenied = null
        onPermissionGranted = null
    }

    /**
     * Returns [permission] which are also [requiredPermissions] and haven't
     * yet been granted
     */
    private val grantRequired: Boolean
        get() = requiredPermissions.contains(permission) && !activity.permissionGranted(permission)

    private fun Activity.permissionGranted(permission: String): Boolean =
        checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

    private fun Activity.permissionDeniedSnacky(): Snacky.Builder =
        getSnackyBuilder(permissionDeniedMessage)
            .setIcon(R.drawable.ic_error_24)

    private fun Activity.permissionRequestingSuppressedSnacky(): Snacky.Builder =
        getSnackyBuilder(permissionRequestingSuppressedMessage)
            .setIcon(R.drawable.ic_error_24)
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
}

private fun Activity.getSnackyBuilder(text: CharSequence): Snacky.Builder =
    Snacky.builder()
        .setText(text)
        .centerText()
        .setDuration(Snacky.LENGTH_LONG)
        .setActivity(this)
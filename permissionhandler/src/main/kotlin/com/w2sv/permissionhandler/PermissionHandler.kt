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
    private val permission: String,
    private val activity: ComponentActivity,
    private val permissionDeniedMessage: String,
    private val permissionRequestingSuppressedMessage: String
) : ActivityCallContractAdministrator<String, Boolean>(
    activity,
    ActivityResultContracts.RequestPermission()
) {

    override val key: String = "${this::class.java.name}.$permission"

    /**
     * Display snacky if some permission hasn't been granted,
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
     * Function wrapper either directly running [onGranted] if permission granted,
     * otherwise sets [onGranted] and launches [activityResultCallback]
     */
    fun requestPermission(onGranted: () -> Unit, onDenied: (() -> Unit)? = null) {
        if (!grantRequired)
            onGranted()
        else {
            onPermissionGranted = onGranted
            onPermissionDenied = onDenied

            activityResultLauncher.launch(permission)
        }
    }

    /**
     * Temporary callable to be set before, and to be cleared on exiting of [activityResultCallback]
     */
    private var onPermissionGranted: (() -> Unit)? = null
    private var onPermissionDenied: (() -> Unit)? = null

    private fun Activity.permissionDeniedSnacky(): Snacky.Builder =
        snacky(permissionDeniedMessage)
            .setIcon(R.drawable.ic_error_24)

    private fun Activity.permissionRequestingSuppressedSnacky(): Snacky.Builder =
        snacky(permissionRequestingSuppressedMessage)
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

private fun Activity.snacky(text: CharSequence, duration: Int = Snacky.LENGTH_LONG): Snacky.Builder =
    Snacky.builder()
        .setText(text)
        .centerText()
        .setDuration(duration)
        .setActivity(this)

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
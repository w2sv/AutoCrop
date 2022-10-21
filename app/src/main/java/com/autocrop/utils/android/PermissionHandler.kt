package com.autocrop.utils.android

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.autocrop.utils.android.extensions.show
import com.autocrop.utils.android.extensions.snacky
import com.autocrop.utils.kotlin.BlankFun
import com.w2sv.autocrop.R
import de.mateware.snacky.Snacky
import de.paul_woitaschek.slimber.i

fun Iterator<PermissionHandler?>.requestPermissions(onGranted: BlankFun, onDenied: BlankFun? = null){
    if (!hasNext())
        onGranted()
    else{
        next()?.requestPermission(
            onGranted = {requestPermissions(onGranted, onDenied)},
            onDenied = onDenied
        ) ?: requestPermissions(onGranted, onDenied)
    }
}

class PermissionHandler(
    private val permission: String,
    private val activity: Activity,
    private val permissionDenialMessage: String,
    private val permissionRequestingSuppressedMessage: String)
        : DefaultLifecycleObserver{

    private lateinit var requestPermission: ActivityResultLauncher<String>

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        requestPermission = (activity as ComponentActivity).activityResultRegistry.register(
            "${this::class.java.name}.$permission",
            owner,
            ActivityResultContracts.RequestPermission(),
            ::onRequestPermissionResult
        )
    }

    /**
     * Returns [permission] which are also [requiredPermissions] and haven't
     * yet been granted
     */
    private val grantRequired: Boolean
        get() = requiredPermissions.contains(permission) && !activity.permissionGranted(permission)

    private fun Activity.permissionGranted(permission: String): Boolean =
        checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

    companion object{
        /**
         * Permissions declared within package Manifest actually required by the api,
         * that is whose maxSdkVersion (also manually declared in Manifest) <= Build version
         */
        private lateinit var requiredPermissions: Set<String>

        fun setRequiredPermissions(context: Context){
            requiredPermissions = context.run {
                (
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong()))
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
     * otherwise sets [onGranted] and launches [requestPermissions]
     */
    fun requestPermission(onGranted: BlankFun, onDenied: BlankFun? = null): Unit =
        if (!grantRequired)
            onGranted()
        else{
            onPermissionGranted = onGranted
            onPermissionDenied = onDenied

            requestPermission.launch(permission)
        }

    /**
     * Temporary callable to be set before, and to be cleared on exiting of [onRequestPermissionResult]
     */
    private var onPermissionGranted: BlankFun? = null
    private var onPermissionDenied: BlankFun? = null

    /**
     * Display snacky if some permission hasn't been granted,
     * otherwise run previously set [onPermissionGranted]
     */
    private fun onRequestPermissionResult(permissionGranted: Boolean){
        if (!permissionGranted){
            activity.run {
                if (shouldShowRequestPermissionRationale(permission))
                    permissionDeniedSnacky()
                else
                    permissionRequestingSuppressedSnacky()
            }
                .show()

            onPermissionDenied?.invoke()
            i{"Denied $permission"}
        }
        else
            onPermissionGranted?.invoke()

        onPermissionDenied = null
        onPermissionGranted = null
    }

    private fun Activity.permissionDeniedSnacky(): Snacky.Builder =
        snacky(permissionDenialMessage)
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
package com.autocrop.utilsandroid

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.w2sv.autocrop.R
import de.mateware.snacky.Snacky
import timber.log.Timber

class PermissionsHandler(
    private val activity: Activity,
    private val requiredPermissions: Array<String>,
    private val snackbarMessageOnPermissionDenial: String,
    private val snackbarMessageOnRequestSuppression: String)
        : DefaultLifecycleObserver{

    private lateinit var requestPermissions: ActivityResultLauncher<Array<String>>

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)

        requestPermissions = (activity as ComponentActivity).activityResultRegistry.register(
            "unused",
            owner,
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            onRequestPermissionsResult(it)
        }
    }

    /**
     * Returns [requiredPermissions] which are also [nonRedundantManifestPermissions] and haven't
     * yet been granted
     */
    private val pendingPermissions: List<String>
        get() = requiredPermissions.filter {
            nonRedundantManifestPermissions.contains(it) && !activity.permissionGranted(it)
        }

    /**
     * Returns the permissions declared within package Manifest actually required by the api,
     * that is whose maxSdkVersion (also manually declared in Manifest) <= Build version
     */
    private val nonRedundantManifestPermissions: Array<String> by lazy {
        activity.run {
            packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
                .requestedPermissions ?: arrayOf()
        }
    }

    /**
     * Function wrapper either directly running [onPermissionsGranted] if all permissions granted, otherwise sets
     * [onPermissionsGranted] to [onPermissionsGranted] and launches [requestPermissions]
     */
    fun requestPermissionsOrRun(onPermissionsGranted: () -> Unit): Unit =
        if (pendingPermissions.isEmpty())
            onPermissionsGranted()
        else{
            this.onPermissionsGranted = onPermissionsGranted
            requestPermissions.launch(pendingPermissions.toTypedArray())
        }

    /**
     * Temporary callable to be set before, and to be cleared on exiting of [onRequestPermissionsResult]
     */
    private var onPermissionsGranted: (() -> Unit)?  = null

    /**
     * Display snacky if some permission hasn't been granted,
     * otherwise run previously set [onPermissionsGranted]
     */
    private fun onRequestPermissionsResult(permission2Granted: Map<String, Boolean>){
        if (permission2Granted.values.any { !it }){
            activity.run {
                if (shouldShowRequestPermissionRationale)
                    permissionDeniedSnacky()
                else
                    permissionRequestingSuppressedSnacky()
            }
                .buildAndShow()

            Timber.i("Not all required permissions were granted: $permission2Granted")
        }
        else
            onPermissionsGranted!!()

        onPermissionsGranted = null
    }

    private val shouldShowRequestPermissionRationale: Boolean
        get() = activity.shouldShowRequestPermissionRationale(pendingPermissions[0])

    private fun Activity.permissionDeniedSnacky(): Snacky.Builder =
        snacky(snackbarMessageOnPermissionDenial)
            .setIcon(R.drawable.ic_error_24)

    private fun Activity.permissionRequestingSuppressedSnacky(): Snacky.Builder =
        snacky(snackbarMessageOnRequestSuppression)
            .setIcon(R.drawable.ic_error_24)
            .setActionText("Settings")
            .setActionClickListener {
                activity.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .apply {
                            data = Uri.fromParts("package", activity.packageName, null)
                        }
                )
            }

    private fun Activity.permissionGranted(permission: String): Boolean =
        checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
}
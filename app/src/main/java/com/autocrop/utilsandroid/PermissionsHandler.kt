package com.autocrop.utilsandroid

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.w2sv.autocrop.R
import de.mateware.snacky.Snacky
import timber.log.Timber

class PermissionsHandler(
    private val fragment: Fragment,
    private val requiredPermissions: Array<String>,
    private val snackbarMessageOnPermissionDenial: String,
    private val snackbarMessageOnRequestSuppression: String){

    /**
     * Returns [requiredPermissions] which are also [nonRedundantManifestPermissions] and haven't
     * yet been granted
     */
    private val pendingPermissions: List<String>
        get() = requiredPermissions.filter {
            nonRedundantManifestPermissions.contains(it) && !fragment.requireActivity().permissionGranted(it)
        }

    /**
     * Returns the permissions declared within package Manifest actually required by the api,
     * that is whose maxSdkVersion (also manually declared in Manifest) <= Build version
     */
    private val nonRedundantManifestPermissions: Array<String> by lazy {
        fragment.requireContext().run {
            packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
                .requestedPermissions ?: arrayOf()
        }
    }

    /**
     * Function wrapper either directly running [onPermissionsGranted] if all permissions granted, otherwise sets
     * [onPermissionsGranted] to [onPermissionsGranted] and launches [permissionRequestContract]
     */
    operator fun invoke(onPermissionsGranted: () -> Unit): Unit =
        if (pendingPermissions.isEmpty())
            onPermissionsGranted()
        else{
            this.onPermissionsGranted = onPermissionsGranted
            permissionRequestContract.launch(pendingPermissions.toTypedArray())
        }

    private val permissionRequestContract = fragment.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ){ onRequestPermissionsResult(it) }

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
            fragment.requireActivity().run {
                if (shouldShowRequestPermissionRationale)
                    permissionDeniedSnacky()
                else
                    permissionRequestingSuppressedSnacky()
            }
                .show()

            Timber.i("Not all required permissions were granted: $permission2Granted")
        }
        else
            onPermissionsGranted!!()

        onPermissionsGranted = null
    }

    private val shouldShowRequestPermissionRationale: Boolean
        get() = fragment.shouldShowRequestPermissionRationale(pendingPermissions[0])

    private fun Activity.permissionDeniedSnacky(): Snacky.Builder =
        snacky(
            snackbarMessageOnPermissionDenial,
            R.drawable.ic_error_24
        )

    private fun Activity.permissionRequestingSuppressedSnacky(): Snacky.Builder =
        snacky(
            snackbarMessageOnRequestSuppression,
            R.drawable.ic_error_24
        )
            .setActionText("Settings")
            .setActionClickListener {
                fragment.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .apply {
                            data = Uri.fromParts("package", fragment.requireContext().packageName, null)
                        }
                )
            }

    private fun Activity.permissionGranted(permission: String): Boolean =
        checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
}
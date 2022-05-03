package com.autocrop.activities.main.fragments.flowfield

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.autocrop.utils.android.permissionGranted
import com.autocrop.utils.android.show
import com.autocrop.utils.android.snackbar
import com.w2sv.autocrop.R
import timber.log.Timber

class PermissionsHandler(
    private val fragment: Fragment,
    private val requiredPermissions: Array<String>,
    private val snackbarMessageOnPermissionDenial: String,
    private val snackbarMessageOnRequestSuppression: String){

    private val missingPermissions: List<String>
        get() = requiredPermissions.filter {
            permissionsDeclaredInManifest.contains(it) && !fragment.requireActivity().permissionGranted(it)
        }

    private val permissionsDeclaredInManifest: Array<String> by lazy {
        fragment.requireContext().run {
            packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
                .requestedPermissions ?: arrayOf()
        }
    }

    /**
     * Decorator either directly running [onPermissionsGranted] if all permissions granted, otherwise sets
     * [onPermissionsGranted] to passed function and calls [permissionRequestContract].launch
     */
    operator fun invoke(onPermissionsGranted: () -> Unit): Unit =
        if (missingPermissions.isEmpty())
            onPermissionsGranted()
        else{
            this.onPermissionsGranted = onPermissionsGranted
            permissionRequestContract.launch(missingPermissions.toTypedArray())
        }

    private val permissionRequestContract = fragment.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ){ onRequestPermissionsResult(it) }

    /**
     * Display snackbar if any permission hasn't been granted,
     * otherwise run [onPermissionsGranted], which needs to have been set previously
     *.show()
     * Clears [onPermissionsGranted] afterwards in any case
     */
    private fun onRequestPermissionsResult(permission2Granted: Map<String, Boolean>) {
        if (permission2Granted.values.any { !it }) {
            if (shouldShowRequestPermissionRationale)
                permissionDeniedPrompt()
            else
                permissionRequestingSuppressedPrompt()

            Timber.i("Not all required permissions were granted: $permission2Granted")
        }
        else
            onPermissionsGranted!!()
        onPermissionsGranted = null
    }

    private val shouldShowRequestPermissionRationale: Boolean
        get() = fragment.shouldShowRequestPermissionRationale(missingPermissions[0])

    private var onPermissionsGranted: (() -> Unit)?  = null

    private fun permissionDeniedPrompt() =
        fragment.requireActivity()
            .snackbar(
                snackbarMessageOnPermissionDenial,
                R.drawable.ic_error_24
            )
            .show()

    private fun permissionRequestingSuppressedPrompt() =
        fragment.requireActivity()
            .snackbar(
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
            .show()
}
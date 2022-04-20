package com.autocrop.activities.main.fragments.flowfield

import android.Manifest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.autocrop.utils.android.NotificationColor
import com.autocrop.utils.android.displaySnackbar
import com.autocrop.utils.android.permissionGranted
import com.w2sv.autocrop.R
import timber.log.Timber

class PermissionsHandler(private val fragment: Fragment){

    private companion object{
        val REQUIRED_PERMISSIONS: Array<String> = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    private val missingPermissions: List<String>
        get() = REQUIRED_PERMISSIONS.filter { !fragment.requireActivity().permissionGranted(it) }

    private val permissionRequestContract = fragment.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ){
        onRequestPermissionsResult(it)
    }

    /**
     * Decorator either running passed function if all permissions granted, otherwise sets
     * [onAllPermissionsGranted] to passed function and calls [permissionRequestContract].launch
     */
    fun requestPermissionsIfNecessaryAndOrIfAllGrantedRun(onAllPermissionsGranted: () -> Unit): Unit =
        missingPermissions.let {
            if (it.isNotEmpty()){
                this.onAllPermissionsGranted = onAllPermissionsGranted
                permissionRequestContract.launch(it.toTypedArray())
            }
            else
                onAllPermissionsGranted()
        }

    private var onAllPermissionsGranted: (() -> Unit)?  = null

    /**
     * Display snackbar if any permission hasn't been granted,
     * otherwise run [onAllPermissionsGranted], which needs to have been set previously
     *
     * Clears [onAllPermissionsGranted] afterwards in any case
     */
    private fun onRequestPermissionsResult(permissionToGranted: Map<String, Boolean>) {
        if (permissionToGranted.values.any { !it }) {
            fragment.requireActivity().displaySnackbar(
                "You need to permit media file access in order for the app to work",
                R.drawable.ic_error_24
            )
            Timber.i("Not all required permissions were granted: $permissionToGranted")
        }
        else
            onAllPermissionsGranted!!()
        onAllPermissionsGranted = null
    }
}
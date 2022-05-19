package com.autocrop.activities.main

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import com.autocrop.utilsandroid.PermissionsHandler

class MainActivityViewModel: ViewModel() {
    var fadeInFlowFieldButtons = true

    lateinit var selectImages: ActivityResultLauncher<String>
    lateinit var permissionsHandler: PermissionsHandler
    lateinit var pickSaveDestinationDir: ActivityResultLauncher<Uri?>
}
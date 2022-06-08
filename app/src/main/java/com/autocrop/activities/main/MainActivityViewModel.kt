package com.autocrop.activities.main

import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import com.autocrop.collections.ImageFileIOSynopsis
import com.autocrop.utilsandroid.PermissionsHandler

class MainActivityViewModel(val imageFileIOSynopsis: ImageFileIOSynopsis?): ViewModel() {
    var fadeInFlowFieldButtons = true

    lateinit var selectImages: ActivityResultLauncher<Intent>
    lateinit var permissionsHandler: PermissionsHandler
    lateinit var pickSaveDestinationDir: ActivityResultLauncher<Uri?>
}
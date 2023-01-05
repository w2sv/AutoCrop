package com.w2sv.autocrop.activities.main.fragments.flowfield

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.w2sv.androidutils.ActivityCallContractAdministrator
import com.w2sv.autocrop.cropbundle.io.IMAGE_MIME_TYPE

class SelectImagesContractHandler(
    activity: ComponentActivity,
    override val activityResultCallback: (List<Uri>) -> Unit
) : ActivityCallContractAdministrator<String, List<Uri>>(
    activity,
    ActivityResultContracts.GetMultipleContents()
) {
    fun selectImages() {
        activityResultLauncher.launch(IMAGE_MIME_TYPE)
    }
}
package com.w2sv.autocrop.activities.main.fragments.flowfield

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.w2sv.androidutils.ActivityCallContractAdministrator
import com.w2sv.autocrop.cropbundle.io.IMAGE_MIME_TYPE

class SelectImagesContractHandler(
    activity: ComponentActivity,
    override val activityResultCallback: (ActivityResult) -> Unit
) : ActivityCallContractAdministrator<Intent, ActivityResult>(
    activity,
    ActivityResultContracts.StartActivityForResult()
) {
    fun selectImages() {
        activityResultLauncher.launch(
            Intent(Intent.ACTION_PICK)
                .setType(IMAGE_MIME_TYPE)
                .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        )
    }
}
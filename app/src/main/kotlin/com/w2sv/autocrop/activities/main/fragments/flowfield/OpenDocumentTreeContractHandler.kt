package com.w2sv.autocrop.activities.main.fragments.flowfield

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.w2sv.androidutils.ActivityCallContractAdministrator

class OpenDocumentTreeContractHandler(
    activity: ComponentActivity,
    override val activityResultCallback: (Uri?) -> Unit
) : ActivityCallContractAdministrator<Uri?, Uri?>(
    activity,
    object : ActivityResultContracts.OpenDocumentTree() {
        override fun createIntent(context: Context, input: Uri?): Intent =
            super.createIntent(context, input)
                .setFlags(
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                            Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
                )
    }
) {
    fun selectDocument(treeUri: Uri?) {
        activityResultLauncher.launch(treeUri)
    }
}
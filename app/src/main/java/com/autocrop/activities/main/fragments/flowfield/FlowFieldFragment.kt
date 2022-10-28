package com.autocrop.activities.main.fragments.flowfield

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.text.color
import com.autocrop.activities.cropping.CropActivity
import com.autocrop.activities.main.MainActivity
import com.autocrop.activities.main.fragments.MainActivityFragment
import com.autocrop.preferences.UriPreferences
import com.autocrop.utils.android.documentUriPathIdentifier
import com.autocrop.utils.android.extensions.buildAndShow
import com.autocrop.utils.android.extensions.getThemedColor
import com.autocrop.utils.android.extensions.snackyBuilder
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.FragmentFlowfieldBinding
import com.w2sv.permissionhandler.PermissionHandler

class FlowFieldFragment:
    MainActivityFragment<FragmentFlowfieldBinding>(FragmentFlowfieldBinding::class.java) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycle.addObserver(writeExternalStoragePermissionHandler)
    }

    val writeExternalStoragePermissionHandler by lazy {
        PermissionHandler(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            requireActivity(),
            "Media file writing required for saving crops",
            "Go to app settings and grant media file writing in order for the app to work"
        )
    }

    val imageSelectionIntentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
        activityResult.data?.let { intent ->
            intent.clipData?.let { clipData ->
                startActivity(
                    Intent(
                        requireActivity(),
                        CropActivity::class.java
                    )
                        .putParcelableArrayListExtra(
                            MainActivity.EXTRA_SELECTED_IMAGE_URIS,
                            ArrayList((0 until clipData.itemCount)
                                .map { clipData.getItemAt(it).uri })
                        )
                )
            }
        }
    }

    val saveDestinationSelectionIntentLauncher = registerForActivityResult(object: ActivityResultContracts.OpenDocumentTree(){
        override fun createIntent(context: Context, input: Uri?): Intent =
            super.createIntent(context, input)
                .setFlags(
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                    Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
                )
    }) {
        it?.let { treeUri ->
            if (UriPreferences.treeUri != treeUri){
                UriPreferences.treeUri = treeUri

                requireContext()
                    .contentResolver
                    .takePersistableUriPermission(
                        treeUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                requireActivity().snackyBuilder(
                    SpannableStringBuilder()
                        .append("Crops will be saved to ")
                        .color(requireContext().getThemedColor(R.color.success)){
                            append(
                                documentUriPathIdentifier(UriPreferences.documentUri!!)
                            )
                        }
                )
                    .buildAndShow()
            }
        }
    }
}
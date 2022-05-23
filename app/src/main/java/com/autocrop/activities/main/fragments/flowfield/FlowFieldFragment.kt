package com.autocrop.activities.main.fragments.flowfield

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.text.color
import com.autocrop.activities.IntentExtraIdentifier
import com.autocrop.activities.cropping.CroppingActivity
import com.autocrop.activities.main.fragments.MainActivityFragment
import com.autocrop.global.CropSavingPreferences
import com.autocrop.utilsandroid.*
import com.w2sv.autocrop.databinding.MainFragmentFlowfieldBinding

class FlowFieldFragment:
    MainActivityFragment<MainFragmentFlowfieldBinding>(MainFragmentFlowfieldBinding::class.java) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PermissionsHandler(
            requireActivity(),
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            "You'll have to permit media file access in order for the app to save generated crops",
            "Go to app settings and grant media file access in order for the app to save generated crops"
        ).let {
            sharedViewModel.permissionsHandler = it
            lifecycle.addObserver(it)
        }

        sharedViewModel.selectImages = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            activityResult.data?.let { intent ->
                intent.clipData?.let { clipData ->
                    startActivity(
                        Intent(
                            requireActivity(),
                            CroppingActivity::class.java
                        )
                            .putParcelableArrayListExtra(
                                IntentExtraIdentifier.SELECTED_IMAGE_URIS,
                                ArrayList((0 until clipData.itemCount).map { clipData.getItemAt(it).uri })
                            )
                    )
                }
            }
        }

        sharedViewModel.pickSaveDestinationDir = registerForActivityResult(object: ActivityResultContracts.OpenDocumentTree(){
            override fun createIntent(context: Context, input: Uri?): Intent =
                super.createIntent(context, input)
                    .apply {
                        flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                                Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
                    }
        }) {
            it?.let { treeUri ->
                if (CropSavingPreferences.treeUri != treeUri){
                    CropSavingPreferences.treeUri = treeUri

                    requireContext().contentResolver.takePersistableUriPermission(
                        treeUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    requireActivity().snacky(
                        SpannableStringBuilder()
                            .append("Crops will be saved to ")
                            .color(getColorInt(NotificationColor.SUCCESS, requireContext())){
                                append(
                                    documentUriPathIdentifier(CropSavingPreferences.documentUri!!)
                                )
                            }
                    )
                        .show()
                }
            }
        }
    }
}
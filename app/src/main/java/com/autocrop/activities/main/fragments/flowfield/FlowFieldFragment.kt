package com.autocrop.activities.main.fragments.flowfield

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.text.color
import com.autocrop.activities.IntentExtraIdentifier
import com.autocrop.activities.cropping.CroppingActivity
import com.autocrop.activities.main.fragments.MainActivityFragment
import com.autocrop.activities.main.fragments.about.AboutFragment
import com.autocrop.global.CropFileSaveDestinationPreferences
import com.autocrop.utilsandroid.*
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.MainFragmentFlowfieldBinding

class FlowFieldFragment:
    MainActivityFragment<MainFragmentFlowfieldBinding>(MainFragmentFlowfieldBinding::class.java) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // set button onClickListeners
        setImageSelectionButtonOnClickListener()
        setMenuInflationButtonOnClickListener()
    }

    //$$$$$$$$$$$$$$$
    // Menu-related $
    //$$$$$$$$$$$$$$$

    private fun setMenuInflationButtonOnClickListener() =
        binding.menuInflationButton.setOnClickListener { popupMenu.show() }

    private val popupMenu by lazy {
        FlowFieldFragmentMenu(
            mapOf(
                R.id.main_menu_item_change_save_destination_dir to ::pickCropSaveDestinationDir,
                R.id.main_menu_item_rate_the_app to ::goToPlayStoreListing,
                R.id.main_menu_item_about_the_app to ::invokeAboutFragment
            ),
            requireContext(),
            binding.menuInflationButton
        )
    }

    private fun pickCropSaveDestinationDir() =
        pickSaveDestinationDirContract.launch(CropFileSaveDestinationPreferences.treeUri)

    private val pickSaveDestinationDirContract = registerForActivityResult(
        object: ActivityResultContracts.OpenDocumentTree(){
            override fun createIntent(context: Context, input: Uri?): Intent =
                super.createIntent(context, input)
                    .apply {
                        flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                                Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
                    }
        }
    ) {
        it?.let { treeUri ->
            if (CropFileSaveDestinationPreferences.treeUri != treeUri){
                CropFileSaveDestinationPreferences.treeUri = treeUri

                with(requireActivity()){
                    applicationContext.contentResolver.takePersistableUriPermission(
                        treeUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    snacky(
                        SpannableStringBuilder()
                            .append("Crops will be saved to ")
                            .color(getColorInt(NotificationColor.SUCCESS, requireContext())){
                                append(
                                    documentUriPathIdentifier(CropFileSaveDestinationPreferences.documentUri!!)
                                )
                            }
                    )
                        .show()
                }
            }
        }
    }

    private fun invokeAboutFragment() =
        typedActivity.replaceCurrentFragmentWith(AboutFragment(), false)

    private fun goToPlayStoreListing() =
        try{
            startActivity(
                Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://play.google.com/store/apps/details?id=${requireContext().packageName}")
                    setPackage("com.android.vending")
                }
            )
        } catch (e: ActivityNotFoundException){
            requireActivity()
                .snacky("Seems like you're not signed into the Play Store, pal \uD83E\uDD14")
                .show()
        }

    //$$$$$$$$$$$$$$$$$$
    // Image Selection $
    //$$$$$$$$$$$$$$$$$$

    /**
     * Launch [selectImages] if all permissions granted, otherwise request required permissions and
     * then launch [selectImages] if all granted
     */
    private fun setImageSelectionButtonOnClickListener() = binding.imageSelectionButton.setOnClickListener {
        requestWritePermissionOrRun {
            selectImages()
        }
    }

    private val requestWritePermissionOrRun: PermissionsHandler =
        PermissionsHandler(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            "You'll have to permit media file access in order for the app to save generated crops",
            "Go to app settings and grant media file access in order for the app to save generated crops"
        )

    private fun selectImages() =
        @Suppress("DEPRECATION")
        startActivityForResult(
            Intent(Intent.ACTION_PICK)
                .apply {
                    type = MimeTypes.IMAGE
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                },
            IntentCode.IMAGE_SELECTION.ordinal
        )

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && requestCode == IntentCode.IMAGE_SELECTION.ordinal)
            startActivity(
                Intent(
                    requireActivity(),
                    CroppingActivity::class.java
                )
                    .putParcelableArrayListExtra(
                        IntentExtraIdentifier.SELECTED_IMAGE_URIS,
                        ArrayList(data?.clipDataItems()!!.map { it.uri })
                    )
            )
    }

    private enum class IntentCode {
        IMAGE_SELECTION
    }
}
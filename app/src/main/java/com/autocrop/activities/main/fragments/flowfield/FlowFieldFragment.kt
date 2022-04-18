package com.autocrop.activities.main.fragments.flowfield

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
import com.autocrop.activities.IntentIdentifier
import com.autocrop.activities.cropping.CroppingActivity
import com.autocrop.activities.main.fragments.MainActivityFragment
import com.autocrop.global.CropFileSaveDestinationPreferences
import com.autocrop.uicontroller.fragment.ActivityRootFragment
import com.autocrop.utils.android.*
import com.autocrop.utils.numberInflection
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.ActivityMainFragmentFlowfieldBinding

class FlowFieldFragment:
    MainActivityFragment<ActivityMainFragmentFlowfieldBinding>(),
    ActivityRootFragment {

    private val permissionsHandler = PermissionsHandler(this)
    private lateinit var flowFieldHandler: FlowFieldHandler

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // initialize FlowFieldHandler and thus FlowField
        flowFieldHandler = FlowFieldHandler(requireActivity(), binding.canvasContainer)

        // set button onClickListeners
        setImageSelectionButtonOnClickListener()
        setMenuInflationButtonOnClickListener()
        flowFieldHandler.setFlowFieldCaptureButton(binding.flowfieldCaptureButton, permissionsHandler)

        // display CropIOResultSnackbar
        displayActivityEntrySnackbar()
    }

    private val nSavedCropsRetriever = IntentExtraRetriever<IntArray>(IntentIdentifier.N_SAVED_CROPS_WITH_N_DELETED_SCREENSHOTS)
    private val cropWriteDirPathRetriever = IntentExtraRetriever<String>(IntentIdentifier.CROP_WRITE_DIR_PATH)

    override fun displayActivityEntrySnackbar(){
        nSavedCropsRetriever(requireActivity().intent)?.let {
            val (nSavedCrops, nDeletedScreenshots) = it[0] to it[1]

            when (nSavedCrops) {
                0 -> requireActivity().displaySnackbar("Discarded all crops", R.drawable.ic_outline_sentiment_dissatisfied_24)
                else ->
                    requireActivity().displaySnackbar(
                        SpannableStringBuilder().apply {
                            append("Saved $nSavedCrops crop${numberInflection(nSavedCrops)} to ")
                            color(getColorInt(NotificationColor.SUCCESS, requireContext())){append(cropWriteDirPathRetriever(requireActivity().intent)!!)}
                            if (nDeletedScreenshots != 0)
                                append(" and deleted ${if (nDeletedScreenshots == nSavedCrops) "corresponding" else nDeletedScreenshots} screenshot${numberInflection(nDeletedScreenshots)}")
                        },
                        R.drawable.ic_baseline_done_24
                    )
            }
        }
    }

    //$$$$$$$$$$$$$$$
    // Menu-related $
    //$$$$$$$$$$$$$$$

    private fun setMenuInflationButtonOnClickListener() =
        binding.menuButton.setOnClickListener { view: View ->
                FlowFieldFragmentMenu(
                    mapOf(
                        R.id.main_menu_item_change_save_destination_dir to {
                            pickSaveDestinationDirContract.launch(CropFileSaveDestinationPreferences.treeUri)
                        },
                        R.id.main_menu_item_rate_the_app to ::goToPlayStoreListing,
                        R.id.main_menu_item_about_the_app to {
                            with(typedActivity) {
                                swapFragments(rootFragment, aboutFragment)
                            }
                        }
                    ),
                    requireContext(),
                    view
                )
                .show()
        }

    private fun goToPlayStoreListing() =
        try{
            startActivity(
                Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://play.google.com/store/apps/details?id=${requireContext().packageName}")
                    setPackage("com.android.vending")
                }
            )
        } catch (e: ActivityNotFoundException){
            requireActivity().displaySnackbar("Seems like you're not signed into the Play Store, pal \uD83E\uDD14")
        }

    private val pickSaveDestinationDirContract = registerForActivityResult(
        object: ActivityResultContracts.OpenDocumentTree(){
            override fun createIntent(context: Context, input: Uri?): Intent {
                return super.createIntent(context, input)
                    .apply {
                        flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
                    }
            }
        }
    ) {
        it?.let { treeUri ->
            requireActivity().applicationContext.contentResolver.takePersistableUriPermission(treeUri,Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            CropFileSaveDestinationPreferences.treeUri = treeUri
        }
    }

    //$$$$$$$$$$$$$$$$$$
    // Image Selection $
    //$$$$$$$$$$$$$$$$$$

    /**
     * Launch [selectImages] if all permissions granted, otherwise request required permissions and
     * then launch [selectImages] if all granted
     */
    private fun setImageSelectionButtonOnClickListener() = binding.imageSelectionButton.setOnClickListener {
        permissionsHandler.requestPermissionsIfNecessaryAndOrIfAllGrantedRun {
            selectImages()
        }
    }

    @Suppress("DEPRECATION")
    private fun selectImages() =
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

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                IntentCode.IMAGE_SELECTION.ordinal -> {
                    startActivity(
                        Intent(
                            requireActivity(),
                            CroppingActivity::class.java
                        )
                            .putParcelableArrayListExtra(
                                IntentIdentifier.SELECTED_IMAGE_URIS,
                                ArrayList(data?.clipDataItems()!!.map { it.uri })
                            )
                    )
                }
            }
        }
    }

    private enum class IntentCode {
        IMAGE_SELECTION
    }

    //$$$$$$$$$$$$$$$$$
    // After Creation $
    //$$$$$$$$$$$$$$$$$

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)

        if (hidden)
            flowFieldHandler.flowfield.pause()
        else
            flowFieldHandler.flowfield.resume()
    }
}
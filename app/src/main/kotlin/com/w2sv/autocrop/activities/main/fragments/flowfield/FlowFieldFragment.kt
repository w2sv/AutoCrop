package com.w2sv.autocrop.activities.main.fragments.flowfield

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.androidutils.extensions.getColoredIcon
import com.w2sv.androidutils.extensions.getLong
import com.w2sv.androidutils.extensions.getThemedColor
import com.w2sv.androidutils.extensions.launchDelayed
import com.w2sv.androidutils.extensions.postValue
import com.w2sv.androidutils.extensions.show
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.ApplicationFragment
import com.w2sv.autocrop.activities.crop.CropActivity
import com.w2sv.autocrop.activities.cropexamination.CropExaminationActivity
import com.w2sv.autocrop.activities.main.MainActivity
import com.w2sv.autocrop.activities.main.fragments.flowfield.dialogs.ScreenshotListenerDialog
import com.w2sv.autocrop.activities.main.fragments.flowfield.dialogs.WelcomeDialog
import com.w2sv.autocrop.cropbundle.io.IMAGE_MIME_TYPE
import com.w2sv.autocrop.databinding.FragmentFlowfieldBinding
import com.w2sv.autocrop.preferences.BooleanPreferences
import com.w2sv.autocrop.preferences.UriPreferences
import com.w2sv.autocrop.screenshotlistening.ScreenshotListener
import com.w2sv.autocrop.ui.SnackbarData
import com.w2sv.autocrop.ui.animate
import com.w2sv.autocrop.ui.fadeIn
import com.w2sv.autocrop.utils.documentUriPathIdentifier
import com.w2sv.autocrop.utils.extensions.snackyBuilder
import com.w2sv.permissionhandler.PermissionHandler
import com.w2sv.permissionhandler.requestPermissions
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FlowFieldFragment :
    ApplicationFragment<FragmentFlowfieldBinding>(FragmentFlowfieldBinding::class.java),
    WelcomeDialog.Listener,
    ScreenshotListenerDialog.Listener {

    @Inject
    lateinit var booleanPreferences: BooleanPreferences

    @Inject
    lateinit var uriPreferences: UriPreferences

    class ViewModel : androidx.lifecycle.ViewModel() {
        var fadedInButtons: Boolean = false
        var showedSnackbar: Boolean = false
    }

    private val viewModel by viewModels<ViewModel>()

    private val activityViewModel by activityViewModels<MainActivity.ViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycle.addObserver(selectImagesContractHandler)
        lifecycle.addObserver(openDocumentTreeContractHandler)

        lifecycle.addObserver(writeExternalStoragePermissionHandler)
        screenshotListeningPermissionHandlers.forEach(lifecycle::addObserver)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showUIElements()

        if (!booleanPreferences.welcomeDialogsShown)
            lifecycleScope.launchDelayed(resources.getLong(R.integer.delay_large)) {
                WelcomeDialog().show(childFragmentManager)
            }
        else if (activityViewModel.followingCropExaminationActivity && !viewModel.showedSnackbar) {
            lifecycleScope.launchDelayed(resources.getLong(R.integer.duration_flowfield_buttons_half_faded_in)) {
                with(activityViewModel.ioResults!!.snackbarData(requireContext())) {
                    requireActivity()
                        .snackyBuilder(text)
                        .setIcon(icon)
                        .build()
                        .show()
                }
                viewModel.showedSnackbar = true
            }
        }

        binding.setOnClickListeners()
    }

    private fun showUIElements(){
        val fadeInButtons: List<View> = listOf(
            binding.navigationDrawerButtonBurger,
            binding.imageSelectionButton
        )

        if (!viewModel.fadedInButtons) {
            fadeInButtons.forEach {
                it.fadeIn(resources.getLong(R.integer.duration_flowfield_buttons_fade_in))
            }

            if (activityViewModel.savedCrops)
                lifecycleScope.launchDelayed(resources.getLong(R.integer.duration_flowfield_buttons_half_faded_in)) {
                    with(binding.shareCropsButton) {
                        alpha = 0f
                        show()
                        animate(Techniques.RotateInUpLeft)
                    }
                }

            viewModel.fadedInButtons = true
        }
        else {
            fadeInButtons.forEach {
                it.show()
            }
            if (activityViewModel.savedCrops)
                binding.shareCropsButton.show()
        }
    }

    private fun FragmentFlowfieldBinding.setOnClickListeners() {
        imageSelectionButton.setOnClickListener {
            writeExternalStoragePermissionHandler.requestPermission(
                onGranted = selectImagesContractHandler::selectImages
            )
        }
        shareCropsButton.setOnClickListener {
            startActivity(
                Intent.createChooser(
                    Intent(Intent.ACTION_SEND_MULTIPLE)
                        .putExtra(
                            Intent.EXTRA_STREAM,
                            activityViewModel.ioResults!!.cropUris
                        )
                        .setType(IMAGE_MIME_TYPE),
                    null
                )
            )
        }
    }

    // $$$$$$$$$$$$$$$$$$
    // Dialog Listeners
    // $$$$$$$$$$$$$$$$$$

    override fun onWelcomeDialogClosedListener() {
        ScreenshotListenerDialog().show(childFragmentManager)
    }

    override fun onScreenshotListenerDialogConfirmedListener() {
        screenshotListeningPermissionHandlers
            .requestPermissions(
                onGranted = {
                    ScreenshotListener.startService(requireContext())
                    activityViewModel.liveScreenshotListenerRunning.postValue(true)
                }
            )
    }

    override fun onScreenshotListenerDialogAnsweredListener() {
        booleanPreferences.welcomeDialogsShown = true
    }

    // $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
    // ActivityCallContractAdministrators
    // $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$

    private val writeExternalStoragePermissionHandler by lazy {
        PermissionHandler(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            requireActivity(),
            "Media file writing required for saving crops",
            "Go to app settings and grant media file writing in order for the app to work"
        )
    }

    val screenshotListeningPermissionHandlers by lazy {
        buildList {
            add(
                PermissionHandler(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        Manifest.permission.READ_MEDIA_IMAGES
                    else
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                    requireActivity(),
                    "Media file access required for listening to screen captures",
                    "Go to app settings and grant media file access for screen capture listening to work"
                )
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                add(
                    PermissionHandler(
                        Manifest.permission.POST_NOTIFICATIONS,
                        requireActivity(),
                        "If you don't allow for the posting of notifications AutoCrop can't inform you about croppable screenshots",
                        "Go to app settings and enable notification posting for screen capture listening to work"
                    )
                )
        }
    }

    private val selectImagesContractHandler by lazy {
        SelectImagesContractHandler(requireActivity()) { imageUris ->
            if (imageUris.isNotEmpty())
                requireActivity().startActivity(
                    Intent(
                        activity,
                        CropActivity::class.java
                    )
                        .putParcelableArrayListExtra(
                            MainActivity.EXTRA_SELECTED_IMAGE_URIS,
                            ArrayList(imageUris)
                        )
                )
        }
    }

    val openDocumentTreeContractHandler by lazy {
        OpenDocumentTreeContractHandler(requireActivity()) {
            it?.let { treeUri ->
                if (uriPreferences.treeUri != treeUri) {
                    uriPreferences.treeUri = treeUri

                    requireContext()
                        .contentResolver
                        .takePersistableUriPermission(
                            treeUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                    requireActivity()
                        .snackyBuilder(
                            SpannableStringBuilder()
                                .append("Crops will be saved to ")
                                .color(requireContext().getThemedColor(R.color.success)) {
                                    append(documentUriPathIdentifier(uriPreferences.documentUri!!))
                                }
                        )
                        .build()
                        .show()
                }
            }
        }
    }
}

private fun CropExaminationActivity.Results.snackbarData(context: Context): SnackbarData =
    if (nSavedCrops == 0)
        SnackbarData("Discarded all crops")
    else
        SnackbarData(
            buildSpannedString {
                append(
                    "Saved $nSavedCrops crop(s) to "
                )
                color(context.getThemedColor(R.color.success)) {
                    append(saveDirName)
                }
                if (nDeletedScreenshots != 0)
                    append(
                        " and deleted ${
                            if (nDeletedScreenshots == nSavedCrops)
                                "corresponding"
                            else
                                nDeletedScreenshots
                        } screenshot(s)"
                    )
            },
            context.getColoredIcon(R.drawable.ic_check_24, R.color.success)
        )
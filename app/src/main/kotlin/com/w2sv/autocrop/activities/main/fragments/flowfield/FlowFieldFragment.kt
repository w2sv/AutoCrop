package com.w2sv.autocrop.activities.main.fragments.flowfield

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.androidutils.BackPressListener
import com.w2sv.androidutils.extensions.getColoredIcon
import com.w2sv.androidutils.extensions.getLong
import com.w2sv.androidutils.extensions.getThemedColor
import com.w2sv.androidutils.extensions.launchDelayed
import com.w2sv.androidutils.extensions.postValue
import com.w2sv.androidutils.extensions.show
import com.w2sv.androidutils.extensions.uris
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.ApplicationFragment
import com.w2sv.autocrop.activities.crop.CropActivity
import com.w2sv.autocrop.activities.examination.IOResults
import com.w2sv.autocrop.activities.main.MainActivity
import com.w2sv.autocrop.activities.main.fragments.flowfield.dialogs.ScreenshotListenerDialog
import com.w2sv.autocrop.activities.main.fragments.flowfield.dialogs.WelcomeDialog
import com.w2sv.autocrop.cropbundle.io.IMAGE_MIME_TYPE
import com.w2sv.autocrop.databinding.FragmentFlowfieldBinding
import com.w2sv.autocrop.preferences.CropSaveDirPreferences
import com.w2sv.autocrop.preferences.ShownFlags
import com.w2sv.autocrop.screenshotlistening.ScreenshotListener
import com.w2sv.autocrop.ui.SnackbarData
import com.w2sv.autocrop.ui.animate
import com.w2sv.autocrop.ui.fadeIn
import com.w2sv.autocrop.utils.extensions.snackyBuilder
import com.w2sv.autocrop.utils.getMediaUri
import com.w2sv.permissionhandler.PermissionHandler
import com.w2sv.permissionhandler.requestPermissions
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.mateware.snacky.Snacky
import javax.inject.Inject

@AndroidEntryPoint
class FlowFieldFragment :
    ApplicationFragment<FragmentFlowfieldBinding>(FragmentFlowfieldBinding::class.java),
    WelcomeDialog.Listener,
    ScreenshotListenerDialog.Listener {

    companion object {
        fun getInstance(ioResults: IOResults?): FlowFieldFragment =
            FlowFieldFragment().apply {
                arguments = bundleOf(IOResults.EXTRA to ioResults)
            }
    }

    @Inject
    lateinit var shownFlags: ShownFlags

    @Inject
    lateinit var cropSaveDirPreferences: CropSaveDirPreferences

    @HiltViewModel
    class ViewModel @Inject constructor(
        savedStateHandle: SavedStateHandle,
        cropSaveDirPreferences: CropSaveDirPreferences,
        @ApplicationContext context: Context
    ) : androidx.lifecycle.ViewModel() {

        val ioResults: IOResults? = savedStateHandle[IOResults.EXTRA]

        val ioResultsSnackbarData: SnackbarData? = ioResults?.let {
            if (it.nSavedCrops == 0)
                SnackbarData("Discarded all crops")
            else
                SnackbarData(
                    buildSpannedString {
                        append(
                            "Saved ${it.nSavedCrops} crop(s) to "
                        )
                        color(context.getThemedColor(R.color.success)) {
                            append(cropSaveDirPreferences.pathIdentifier)
                        }
                        if (it.nDeletedScreenshots != 0)
                            append(
                                " and deleted ${
                                    if (it.nDeletedScreenshots == it.nSavedCrops)
                                        "corresponding"
                                    else
                                        it.nDeletedScreenshots
                                } screenshot(s)"
                            )
                    },
                    context.getColoredIcon(R.drawable.ic_check_24, R.color.success)
                )
        }

        val followingExaminationActivity: Boolean = ioResults != null

        var fadedInButtons: Boolean = false
        var showedSnackbar: Boolean = false

        val liveCropSaveDirIdentifier: LiveData<String> = MutableLiveData(cropSaveDirPreferences.pathIdentifier)

        val backPressHandler = BackPressListener(
            viewModelScope,
            context.resources.getLong(R.integer.duration_backpress_confirmation_window)
        )
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

        showLayoutElements()

        if (!shownFlags.welcomeDialogsShown)
            lifecycleScope.launchDelayed(resources.getLong(R.integer.delay_large)) {
                WelcomeDialog().show(childFragmentManager)
            }
        else if (viewModel.followingExaminationActivity && !viewModel.showedSnackbar) {
            lifecycleScope.launchDelayed(resources.getLong(R.integer.duration_flowfield_buttons_half_faded_in)) {
                with(viewModel.ioResultsSnackbarData!!) {
                    repelledSnackyBuilder(text)
                        .setIcon(icon)
                        .build()
                        .show()
                }
                viewModel.showedSnackbar = true
            }
        }

        binding.setOnClickListeners()
    }

    private fun showLayoutElements() {
        val fadeInButtons: List<View> = listOf(
            binding.navigationViewToggleButton,
            binding.imageSelectionButton
        )
        val savedAnyCrops: Boolean = viewModel.ioResults?.let { it.nSavedCrops != 0 }
            ?: false

        if (!viewModel.fadedInButtons) {
            fadeInButtons.forEach {
                it.fadeIn(resources.getLong(R.integer.duration_flowfield_buttons_fade_in))
            }

            if (savedAnyCrops)
                lifecycleScope.launchDelayed(resources.getLong(R.integer.duration_flowfield_buttons_half_faded_in)) {
                    with(binding.shareCropsButton) {
                        show()
                        animate(Techniques.RotateInUpLeft)
                    }
                }

            viewModel.fadedInButtons = true
        }
        else if (savedAnyCrops)
            binding.shareCropsButton.show()
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
                            viewModel.ioResults!!.cropUris
                        )
                        .setType(IMAGE_MIME_TYPE),
                    "Share Crops"
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

    // $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
    // ActivityCallContractAdministrators
    // $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$

    private val writeExternalStoragePermissionHandler by lazy {
        PermissionHandler(
            requireActivity(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            "Media file writing required for saving crops",
            "Go to app settings and grant media file writing in order for the app to work"
        )
    }

    val screenshotListeningPermissionHandlers by lazy {
        buildList {
            add(
                PermissionHandler(
                    requireActivity(),
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                        Manifest.permission.READ_MEDIA_IMAGES
                    else
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                    "Media file access required for listening to screen captures",
                    "Go to app settings and grant media file access for screen capture listening to work"
                )
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                add(
                    PermissionHandler(
                        requireActivity(),
                        Manifest.permission.POST_NOTIFICATIONS,
                        "If you don't allow for the posting of notifications AutoCrop can't inform you about croppable screenshots",
                        "Go to app settings and enable notification posting for screen capture listening to work"
                    )
                )
        }
    }

    private val selectImagesContractHandler: SelectImagesContractHandlerCompat<*, *> by lazy {
        SelectImagesContractHandlerCompat.getInstance(
            requireActivity(),
            callbackLowerThanQ = {
                it.uris?.let { uris ->
                    startActivity(
                        Intent(
                            requireActivity(),
                            CropActivity::class.java
                        )
                            .putParcelableArrayListExtra(
                                MainActivity.EXTRA_SELECTED_IMAGE_URIS,
                                ArrayList(uris)
                            )
                    )
                }
            },
            callbackFromQ = @SuppressLint("NewApi") { imageUris ->
                if (imageUris.isNotEmpty()) {
                    if (getMediaUri(requireContext(), imageUris.first()) == null)
                        requireActivity()
                            .snackyBuilder("Content provider not supported. Please use a different one")
                            .setIcon(com.w2sv.permissionhandler.R.drawable.ic_error_24)
                            .build()
                            .show()
                    else
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
        )
    }

    val openDocumentTreeContractHandler by lazy {
        OpenDocumentTreeContractHandler(requireActivity()) {
            it?.let { treeUri ->
                if (cropSaveDirPreferences.setNewUriIfApplicable(treeUri, requireContext().contentResolver)) {
                    viewModel.liveCropSaveDirIdentifier.postValue(cropSaveDirPreferences.pathIdentifier)

                    repelledSnackyBuilder(
                            SpannableStringBuilder()
                                .append("Crops will be saved to ")
                                .color(requireContext().getThemedColor(R.color.success)) {
                                    append(viewModel.liveCropSaveDirIdentifier.value!!)
                                }
                        )
                        .build()
                        .show()
                }
                else
                    repelledSnackyBuilder("Reselected preset directory")
                        .build()
                        .show()
            }
        }
    }

    fun onBackPress(){
        viewModel.backPressHandler(
            {
                repelledSnackyBuilder("Tap again to exit")
                    .build()
                    .show()
            },
            {
                requireActivity().finishAffinity()
            }
        )
    }

    private fun repelledSnackyBuilder(text: CharSequence): Snacky.Builder =
        requireActivity()
            .snackyBuilder(text)
            .setView(binding.snackbarRepelledLayout.parent as View)
}
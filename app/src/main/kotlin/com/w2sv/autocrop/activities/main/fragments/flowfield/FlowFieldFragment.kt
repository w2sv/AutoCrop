package com.w2sv.autocrop.activities.main.fragments.flowfield

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.w2sv.androidutils.BackPressListener
import com.w2sv.androidutils.extensions.getColoredIcon
import com.w2sv.androidutils.extensions.getLong
import com.w2sv.androidutils.extensions.getThemedColor
import com.w2sv.androidutils.extensions.hide
import com.w2sv.androidutils.extensions.hideSystemBars
import com.w2sv.androidutils.extensions.launchDelayed
import com.w2sv.androidutils.extensions.postValue
import com.w2sv.androidutils.extensions.show
import com.w2sv.androidutils.extensions.showSystemBars
import com.w2sv.androidutils.extensions.toggle
import com.w2sv.androidutils.extensions.uris
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.ApplicationFragment
import com.w2sv.autocrop.activities.crop.CropActivity
import com.w2sv.autocrop.activities.examination.IOResults
import com.w2sv.autocrop.activities.main.MainActivity
import com.w2sv.autocrop.activities.main.fragments.flowfield.contracthandlers.OpenDocumentTreeContractHandler
import com.w2sv.autocrop.activities.main.fragments.flowfield.contracthandlers.SelectImagesContractHandlerCompat
import com.w2sv.autocrop.cropbundle.io.IMAGE_MIME_TYPE
import com.w2sv.autocrop.databinding.FragmentFlowfieldBinding
import com.w2sv.autocrop.preferences.CropSaveDirPreferences
import com.w2sv.autocrop.preferences.Flags
import com.w2sv.autocrop.screenshotlistening.ScreenshotListener
import com.w2sv.autocrop.ui.SnackbarData
import com.w2sv.autocrop.ui.animate
import com.w2sv.autocrop.ui.fadeIn
import com.w2sv.autocrop.ui.fadeOut
import com.w2sv.autocrop.utils.extensions.snackyBuilder
import com.w2sv.autocrop.utils.getMediaUri
import com.w2sv.permissionhandler.PermissionHandler
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.mateware.snacky.Snacky
import javax.inject.Inject

@AndroidEntryPoint
class FlowFieldFragment :
    ApplicationFragment<FragmentFlowfieldBinding>(FragmentFlowfieldBinding::class.java) {

    companion object {
        fun getInstance(ioResults: IOResults?): FlowFieldFragment =
            FlowFieldFragment().apply {
                arguments = bundleOf(IOResults.EXTRA to ioResults)
            }
    }

    @Inject
    lateinit var flags: Flags

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

        val hideForegroundLive: LiveData<Boolean> = MutableLiveData(false)
        val hideForegroundTogglingEnabled: Boolean
            get() = foregroundToggleAnimation?.let { !it.isStarted }
                ?: true
        var foregroundToggleAnimation: YoYo.YoYoString? = null

        val backPressHandler = BackPressListener(
            viewModelScope,
            context.resources.getLong(R.integer.duration_backpress_confirmation_window)
        )
    }

    private val viewModel by viewModels<ViewModel>()

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

        if (viewModel.followingExaminationActivity && !viewModel.showedSnackbar) {
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
        viewModel.setLiveDataObservers()
    }

    private fun ViewModel.setLiveDataObservers() {
        hideForegroundLive.observe(viewLifecycleOwner) { hideForeground ->
            binding.highAlphaForegroundLayout.let {
                if (hideForeground) {
                    requireActivity().hideSystemBars()
                    if (lifecycle.currentState == Lifecycle.State.STARTED)
                        it.hide()
                    else
                        foregroundToggleAnimation = it.fadeOut()
                }
                else {
                    requireActivity().showSystemBars()
                    foregroundToggleAnimation = it.fadeIn()
                }
            }
        }
    }

    private fun showLayoutElements() {
        val savedAnyCrops: Boolean = viewModel.ioResults?.let { it.nSavedCrops != 0 }
            ?: false

        if (!viewModel.fadedInButtons) {
            binding.foregroundLayout.fadeIn(resources.getLong(R.integer.duration_flowfield_buttons_fade_in))

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
                onPermissionGranted = selectImagesContractHandler::selectImages
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
        foregroundToggleButton.setOnClickListener {
            if (viewModel.hideForegroundTogglingEnabled)
                viewModel.hideForegroundLive.toggle()
        }
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
        ScreenshotListener.permissionHandlers(requireActivity())
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

    fun onBackPress() {
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
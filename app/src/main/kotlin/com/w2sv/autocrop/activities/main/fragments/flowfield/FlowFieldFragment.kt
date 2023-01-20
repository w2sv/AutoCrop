package com.w2sv.autocrop.activities.main.fragments.flowfield

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.w2sv.androidutils.BackPressListener
import com.w2sv.androidutils.extensions.getColoredIcon
import com.w2sv.androidutils.extensions.getLong
import com.w2sv.androidutils.extensions.getThemedColor
import com.w2sv.androidutils.extensions.hide
import com.w2sv.androidutils.extensions.launchDelayed
import com.w2sv.androidutils.extensions.postValue
import com.w2sv.androidutils.extensions.show
import com.w2sv.androidutils.extensions.toggle
import com.w2sv.androidutils.extensions.uris
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.AppFragment
import com.w2sv.autocrop.activities.crop.CropActivity
import com.w2sv.autocrop.activities.examination.AccumulatedIOResults
import com.w2sv.autocrop.activities.getFragmentInstance
import com.w2sv.autocrop.activities.main.MainActivity
import com.w2sv.autocrop.activities.main.fragments.flowfield.contracthandlers.OpenDocumentTreeContractHandler
import com.w2sv.autocrop.activities.main.fragments.flowfield.contracthandlers.SelectImagesContractHandlerCompat
import com.w2sv.autocrop.cropbundle.io.IMAGE_MIME_TYPE
import com.w2sv.autocrop.databinding.FragmentFlowfieldBinding
import com.w2sv.autocrop.preferences.CropSaveDirPreferences
import com.w2sv.autocrop.preferences.GlobalFlags
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
    AppFragment<FragmentFlowfieldBinding>(FragmentFlowfieldBinding::class.java) {

    companion object {
        fun getInstance(accumulatedIoResults: AccumulatedIOResults?): FlowFieldFragment =
            getFragmentInstance(FlowFieldFragment::class.java, AccumulatedIOResults.EXTRA to accumulatedIoResults)
    }

    @Inject
    lateinit var globalFlags: GlobalFlags

    @Inject
    lateinit var cropSaveDirPreferences: CropSaveDirPreferences

    @HiltViewModel
    class ViewModel @Inject constructor(
        savedStateHandle: SavedStateHandle,
        cropSaveDirPreferences: CropSaveDirPreferences,
        @ApplicationContext context: Context
    ) : androidx.lifecycle.ViewModel() {

        val accumulatedIoResults: AccumulatedIOResults? = savedStateHandle[AccumulatedIOResults.EXTRA]

        val ioResultsSnackbarData: SnackbarData? = accumulatedIoResults?.let {
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

        val followingExaminationActivity: Boolean = accumulatedIoResults != null

        var fadedInButtonsOnCreate: Boolean = false
        var showedSnackbar: Boolean = false

        val liveCropSaveDirIdentifier: LiveData<String> = MutableLiveData(cropSaveDirPreferences.pathIdentifier)

        val hideButtonsLive: LiveData<Boolean> = MutableLiveData(false)
        var buttonFadeAnimation: YoYo.YoYoString? = null

        val backPressHandler = BackPressListener(
            viewModelScope,
            context.resources.getLong(R.integer.duration_backpress_confirmation_window)
        )

        val screenshotListenerCancelledFromNotification: LiveData<Boolean> by lazy {
            MutableLiveData(false)
        }
    }

    private val viewModel by viewModels<ViewModel>()

    class CancelledSSLFromNotificationListener(
        lifecycleOwner: LifecycleOwner,
        private val onReceiveListener: () -> Unit
    ) : BroadcastReceiver(),
        DefaultLifecycleObserver {

        init {
            lifecycleOwner.lifecycle.addObserver(this)

            LocalBroadcastManager
                .getInstance(lifecycleOwner.requireContext())
                .registerReceiver(
                    this,
                    IntentFilter(ScreenshotListener.OnCancelledFromNotificationListener.ACTION_NOTIFY_ON_SCREENSHOT_LISTENER_CANCELLED_LISTENERS)
                )
        }

        override fun onReceive(context: Context?, intent: Intent?) {
            onReceiveListener()
        }

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)

            LocalBroadcastManager
                .getInstance(owner.requireContext())
                .unregisterReceiver(this)
        }

        private fun LifecycleOwner.requireContext(): Context =
            (this as Fragment).requireContext()
    }

    private lateinit var cancelledSSLFromNotificationListener: CancelledSSLFromNotificationListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cancelledSSLFromNotificationListener =
            CancelledSSLFromNotificationListener(this) {
                viewModel.screenshotListenerCancelledFromNotification.postValue(true)
            }

        registerLifecycleObservers(
            buildList {
                add(selectImagesContractHandler)
                add(openDocumentTreeContractHandler)
                add(writeExternalStoragePermissionHandler)
                addAll(screenshotListeningPermissionHandlers)
            }
        )
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
        hideButtonsLive.observe(viewLifecycleOwner) { hideForeground ->
            binding.highAlphaForegroundLayout.let {
                if (hideForeground) {
                    if (lifecycle.currentState == Lifecycle.State.STARTED)
                        it.hide()
                    else {
                        buttonFadeAnimation?.stop()
                        buttonFadeAnimation = it.fadeOut()
                    }
                }
                else {
                    buttonFadeAnimation?.stop()
                    buttonFadeAnimation = it.fadeIn()
                }
            }
        }
    }

    private fun showLayoutElements() {
        val savedAnyCrops: Boolean = viewModel.accumulatedIoResults?.let { it.nSavedCrops != 0 }
            ?: false

        if (!viewModel.fadedInButtonsOnCreate) {
            binding.foregroundLayout.fadeIn(resources.getLong(R.integer.duration_flowfield_buttons_fade_in))

            if (savedAnyCrops)
                lifecycleScope.launchDelayed(resources.getLong(R.integer.duration_flowfield_buttons_half_faded_in)) {
                    with(binding.shareCropsButton) {
                        show()
                        animate(Techniques.RotateInUpLeft)
                    }
                }

            viewModel.fadedInButtonsOnCreate = true
        }
        else if (savedAnyCrops)
            binding.shareCropsButton.show()
    }

    private fun FragmentFlowfieldBinding.setOnClickListeners() {
        navigationViewToggleButton.setOnClickListener {
            with(drawerLayout) {
                if (isOpen)
                    closeDrawer()
                else
                    openDrawer()
            }
        }
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
                            viewModel.accumulatedIoResults!!.cropUris
                        )
                        .setType(IMAGE_MIME_TYPE),
                    "Share Crops"
                )
            )
        }
        foregroundToggleButton.setOnClickListener {
            viewModel.hideButtonsLive.toggle()
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
                if (cropSaveDirPreferences.setNewUri(treeUri, requireContext().contentResolver)) {
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
        binding.drawerLayout.run {
            if (isOpen)
                closeDrawer(GravityCompat.START)
            else
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
    }

    private fun repelledSnackyBuilder(text: CharSequence): Snacky.Builder =
        requireActivity()
            .snackyBuilder(text)
            .setView(binding.snackbarRepelledLayout.parent as View)
}
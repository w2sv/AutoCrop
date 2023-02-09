package com.w2sv.autocrop.activities.main.fragments.flowfield

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.Toast
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.w2sv.androidutils.BackPressHandler
import com.w2sv.androidutils.SelfManagingLocalBroadcastReceiver
import com.w2sv.androidutils.extensions.getColoredDrawable
import com.w2sv.androidutils.extensions.getLong
import com.w2sv.androidutils.extensions.hide
import com.w2sv.androidutils.extensions.postValue
import com.w2sv.androidutils.extensions.show
import com.w2sv.androidutils.extensions.showToast
import com.w2sv.androidutils.extensions.toggle
import com.w2sv.androidutils.extensions.uris
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.AppFragment
import com.w2sv.autocrop.activities.crop.CropActivity
import com.w2sv.autocrop.activities.examination.AccumulatedIOResults
import com.w2sv.autocrop.activities.getFragment
import com.w2sv.autocrop.activities.main.MainActivity
import com.w2sv.autocrop.activities.main.fragments.flowfield.contracthandlers.OpenDocumentTreeContractHandler
import com.w2sv.autocrop.activities.main.fragments.flowfield.contracthandlers.SelectImagesContractHandlerCompat
import com.w2sv.autocrop.cropbundle.io.IMAGE_MIME_TYPE
import com.w2sv.autocrop.databinding.FragmentFlowfieldBinding
import com.w2sv.autocrop.preferences.CropSaveDirPreferences
import com.w2sv.autocrop.screenshotlistening.ScreenshotListener
import com.w2sv.autocrop.ui.SnackbarData
import com.w2sv.autocrop.ui.animate
import com.w2sv.autocrop.ui.fadeIn
import com.w2sv.autocrop.ui.fadeInAnimationComposer
import com.w2sv.autocrop.ui.fadeOut
import com.w2sv.autocrop.ui.onHalfwayFinished
import com.w2sv.autocrop.utils.PermissionHandler
import com.w2sv.autocrop.utils.extensions.onHalfwayShown
import com.w2sv.autocrop.utils.getMediaUri
import com.w2sv.kotlinutils.delegates.AutoSwitch
import com.w2sv.kotlinutils.extensions.numericallyInflected
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.mateware.snacky.Snacky
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

@AndroidEntryPoint
class FlowFieldFragment :
    AppFragment<FragmentFlowfieldBinding>(FragmentFlowfieldBinding::class.java) {

    companion object {
        fun getInstance(accumulatedIoResults: AccumulatedIOResults?): FlowFieldFragment =
            getFragment(FlowFieldFragment::class.java, AccumulatedIOResults.EXTRA to accumulatedIoResults)
    }

    @HiltViewModel
    class ViewModel @Inject constructor(
        savedStateHandle: SavedStateHandle,
        cropSaveDirPreferences: CropSaveDirPreferences,
        @ApplicationContext context: Context
    ) : androidx.lifecycle.ViewModel() {

        val accumulatedIoResults: AccumulatedIOResults? = savedStateHandle[AccumulatedIOResults.EXTRA]

        var fadedInButtonsOnEntry = AutoSwitch(false, switchOn = false)

        /**
         * IO Results Snackbar
         */

        fun showIOResultsSnackbarIfApplicable(
            coroutineScope: CoroutineScope,
            getSnackyBuilder: (CharSequence) -> Snacky.Builder
        ) {
            if (ioResultsSnackbarData != null && !showedSnackbar) {
                getSnackyBuilder(ioResultsSnackbarData.text)
                    .setIcon(ioResultsSnackbarData.icon)
                    .build()
                    .onHalfwayShown(coroutineScope) {
                        showedSnackbar = true
                    }
                    .show()
            }
        }

        private val ioResultsSnackbarData: SnackbarData? = accumulatedIoResults?.let {
            if (it.nSavedCrops == 0)
                SnackbarData("Discarded all crops")
            else
                SnackbarData(
                    buildSpannedString {
                        append(
                            "Saved ${it.nSavedCrops} ${"crop".numericallyInflected(it.nSavedCrops)} to "
                        )
                        color(context.getColor(R.color.success)) {
                            append(cropSaveDirPreferences.pathIdentifier)
                        }
                        if (it.nDeletedScreenshots != 0)
                            append(
                                " and deleted ${
                                    if (it.nDeletedScreenshots == it.nSavedCrops)
                                        "corresponding"
                                    else
                                        it.nDeletedScreenshots
                                } ${"screenshot".numericallyInflected(it.nDeletedScreenshots)}"
                            )
                    },
                    context.getColoredDrawable(R.drawable.ic_check_24, R.color.success)
                )
        }

        private var showedSnackbar: Boolean = false

        /**
         * Button hiding
         */

        val hideButtonsLive: LiveData<Boolean> by lazy {
            MutableLiveData(false)
        }
        var buttonFadeAnimation: YoYo.YoYoString? = null

        /**
         * Other LiveData
         */

        val cropSaveDirIdentifierLive: LiveData<String> by lazy {
            MutableLiveData(cropSaveDirPreferences.pathIdentifier)
        }
        val screenshotListenerCancelledFromNotificationLive: LiveData<Boolean> by lazy {
            MutableLiveData(false)
        }

        /**
         * BackPressListener
         */

        val backPressHandler = BackPressHandler(
            viewModelScope,
            context.resources.getLong(R.integer.duration_backpress_confirmation_window)
        )
    }

    class CancelledSSLFromNotificationListener(
        broadcastManager: LocalBroadcastManager,
        private val onReceiveListener: () -> Unit
    ) : SelfManagingLocalBroadcastReceiver(
        broadcastManager,
        IntentFilter(ScreenshotListener.OnCancelledFromNotificationListener.ACTION_NOTIFY_ON_SCREENSHOT_LISTENER_CANCELLED_LISTENERS)
    ) {

        override fun onReceive(context: Context?, intent: Intent?) {
            onReceiveListener()
        }
    }

    @Inject
    lateinit var cropSaveDirPreferences: CropSaveDirPreferences

    private val viewModel by viewModels<ViewModel>()

    private val cancelledSSLFromNotificationListener: CancelledSSLFromNotificationListener by lazy {
        CancelledSSLFromNotificationListener(LocalBroadcastManager.getInstance(requireContext())) {
            viewModel.screenshotListenerCancelledFromNotificationLive.postValue(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerLifecycleObservers(
            buildList {
                add(cancelledSSLFromNotificationListener)
                add(selectImagesContractHandler)
                add(openDocumentTreeContractHandler)
                add(writeExternalStoragePermissionHandler)
                addAll(screenshotListeningPermissionHandlers)
            }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.showLayoutElements()
        binding.setOnClickListeners()
        viewModel.setLiveDataObservers()
    }

    private fun FragmentFlowfieldBinding.showLayoutElements() {
        val savedAnyCrops: Boolean = viewModel.accumulatedIoResults?.let { it.nSavedCrops != 0 }
            ?: false

        if (!viewModel.fadedInButtonsOnEntry.getWithEventualSwitch()) {
            foregroundLayout
                .fadeInAnimationComposer(resources.getLong(R.integer.duration_flowfield_buttons_fade_in))
                .onHalfwayFinished(lifecycleScope) {
                    viewModel.showIOResultsSnackbarIfApplicable(this, ::getSnackyBuilder)

                    if (savedAnyCrops)
                        with(shareCropsButton) {
                            show()
                            animate(Techniques.RotateInUpLeft)
                        }
                }
                .play()
        }
        else if (savedAnyCrops)
            shareCropsButton.show()
    }

    private fun FragmentFlowfieldBinding.setOnClickListeners() {
        navigationViewToggleButton.setOnClickListener {
            drawerLayout.onToggleButtonClick()
        }
        imageSelectionButton.setOnClickListener {
            writeExternalStoragePermissionHandler.requestPermissionIfRequired(
                onGranted = selectImagesContractHandler::selectImages
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

    private fun ViewModel.setLiveDataObservers() {
        hideButtonsLive.observe(viewLifecycleOwner) { hideForeground ->
            if (hideForeground) {
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

                if (lifecycle.currentState == Lifecycle.State.STARTED)
                    binding.highAlphaForegroundLayout.hide()
                else {
                    buttonFadeAnimation?.stop()
                    buttonFadeAnimation = binding.highAlphaForegroundLayout.fadeOut()
                }
            }
            else {
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)

                buttonFadeAnimation?.stop()
                buttonFadeAnimation = binding.highAlphaForegroundLayout.fadeIn()
            }
        }
    }

    /**
     * ActivityCallContractAdministrators
     */

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
                        requireContext().showToast(
                            "Content provider not supported. Please use a different one",
                            Toast.LENGTH_LONG
                        )
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
                val text = if (cropSaveDirPreferences.setNewUri(treeUri, requireContext().contentResolver)) {
                    viewModel.cropSaveDirIdentifierLive.postValue(cropSaveDirPreferences.pathIdentifier)
                    SpannableStringBuilder()
                        .append("Crops will be saved to ")
                        .color(requireContext().getColor(R.color.success)) {
                            append(viewModel.cropSaveDirIdentifierLive.value!!)
                        }
                }
                else
                    "Reselected preset directory"

                requireContext().showToast(text, Toast.LENGTH_LONG)
            }
        }
    }

    override val snackbarAnchorView: View
        get() = binding.snackbarRepelledLayout.parent as View

    fun onBackPress() {
        binding.drawerLayout.run {
            if (isOpen)
                closeDrawer()
            else
                viewModel.backPressHandler(
                    {
                        requireContext().showToast("Tap again to exit")
                    },
                    {
                        requireActivity().finishAffinity()
                    }
                )
        }
    }
}
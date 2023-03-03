@file: Suppress("DEPRECATION")

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
import com.w2sv.androidutils.BackPressHandler
import com.w2sv.androidutils.SelfManagingLocalBroadcastReceiver
import com.w2sv.androidutils.extensions.getColoredDrawable
import com.w2sv.androidutils.extensions.getLong
import com.w2sv.androidutils.extensions.hide
import com.w2sv.androidutils.extensions.postValue
import com.w2sv.androidutils.extensions.show
import com.w2sv.androidutils.extensions.showToast
import com.w2sv.androidutils.extensions.uris
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.AppFragment
import com.w2sv.autocrop.activities.crop.CropActivity
import com.w2sv.autocrop.domain.AccumulatedIOResults
import com.w2sv.autocrop.activities.main.MainActivity
import com.w2sv.autocrop.activities.main.fragments.flowfield.contracthandlers.OpenDocumentTreeContractHandler
import com.w2sv.autocrop.activities.main.fragments.flowfield.contracthandlers.SelectImagesContractHandlerCompat
import com.w2sv.autocrop.databinding.FlowfieldBinding
import com.w2sv.autocrop.ui.model.SnackbarData
import com.w2sv.autocrop.ui.views.animate
import com.w2sv.autocrop.ui.views.fadeIn
import com.w2sv.autocrop.ui.views.fadeInAnimationComposer
import com.w2sv.autocrop.ui.views.fadeOut
import com.w2sv.autocrop.ui.views.onHalfwayFinished
import com.w2sv.autocrop.utils.getFragment
import com.w2sv.autocrop.utils.getMediaUri
import com.w2sv.autocrop.utils.pathIdentifier
import com.w2sv.common.PermissionHandler
import com.w2sv.cropbundle.io.IMAGE_MIME_TYPE
import com.w2sv.kotlinutils.extensions.numericallyInflected
import com.w2sv.preferences.CropSaveDirPreferences
import com.w2sv.screenshotlistening.ScreenshotListener
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import de.mateware.snacky.Snacky
import javax.inject.Inject

@AndroidEntryPoint
class FlowFieldFragment :
    AppFragment<FlowfieldBinding>(FlowfieldBinding::class.java) {

    companion object {
        fun getInstance(accumulatedIoResults: AccumulatedIOResults?): FlowFieldFragment =
            getFragment(FlowFieldFragment::class.java, AccumulatedIOResults.EXTRA to accumulatedIoResults)
    }

    @HiltViewModel
    class ViewModel @Inject constructor(
        @ApplicationContext context: Context,
        savedStateHandle: SavedStateHandle,
        val cropSaveDirPreferences: CropSaveDirPreferences,
    ) : androidx.lifecycle.ViewModel() {

        val accumulatedIoResults: AccumulatedIOResults? = savedStateHandle[AccumulatedIOResults.EXTRA]

        var fadedInForegroundOnEntry = false

        /**
         * IO Results Snackbar
         */

        fun showIOResultsSnackbarIfApplicable(
            getSnackyBuilder: (CharSequence) -> Snacky.Builder
        ) {
            ioResultsSnackbarData?.let {
                getSnackyBuilder(it.text)
                    .setIcon(it.icon)
                    .build()
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

        /**
         * Misc LiveData
         */

        val hideForegroundElementsLive: LiveData<Boolean> by lazy {
            MutableLiveData(false)
        }

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

    private fun FlowfieldBinding.showLayoutElements() {
        val anyCropsSaved = viewModel.accumulatedIoResults?.anyCropsSaved == true

        when (viewModel.fadedInForegroundOnEntry) {
            true -> if (anyCropsSaved) {
                shareCropsButton.show()
            }

            false -> {
                foregroundLayout
                    .fadeInAnimationComposer(resources.getLong(R.integer.duration_flowfield_buttons_fade_in))
                    .onHalfwayFinished(lifecycleScope) {
                        viewModel.fadedInForegroundOnEntry = true
                        viewModel.showIOResultsSnackbarIfApplicable(::getSnackyBuilder)

                        if (anyCropsSaved) {
                            with(shareCropsButton) {
                                show()
                                animate(Techniques.RotateInUpLeft)
                            }
                        }
                    }
                    .play()
            }
        }
    }

    private fun FlowfieldBinding.setOnClickListeners() {
        navigationViewToggleButton.setOnClickListener {
            drawerLayout.toggleDrawer()
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
    }

    private fun ViewModel.setLiveDataObservers() {
        hideForegroundElementsLive.observe(viewLifecycleOwner) { hideForeground ->
            if (hideForeground) {
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

                if (lifecycle.currentState == Lifecycle.State.STARTED) {
                    binding.highAlphaForegroundLayout.hide()
                }
                else {
                    binding.foregroundElementsToggleButton.setForegroundElementsFadeAnimation {
                        binding.highAlphaForegroundLayout.fadeOut()
                    }
                }
            }
            else {
                binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)

                binding.foregroundElementsToggleButton.setForegroundElementsFadeAnimation {
                    binding.highAlphaForegroundLayout.fadeIn()
                }
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
                val text = if (viewModel.cropSaveDirPreferences.setNewUri(treeUri, requireContext().contentResolver)) {
                    viewModel.cropSaveDirIdentifierLive.postValue(viewModel.cropSaveDirPreferences.pathIdentifier)
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
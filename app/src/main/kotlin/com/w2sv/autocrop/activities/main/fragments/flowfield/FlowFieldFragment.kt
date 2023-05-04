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
import com.w2sv.androidutils.generic.uris
import com.w2sv.androidutils.lifecycle.SelfManagingLocalBroadcastReceiver
import com.w2sv.androidutils.lifecycle.postValue
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.androidutils.ui.resources.getLong
import com.w2sv.androidutils.ui.views.hide
import com.w2sv.androidutils.ui.views.show
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.AppFragment
import com.w2sv.autocrop.activities.crop.CropActivity
import com.w2sv.autocrop.activities.main.MainActivity
import com.w2sv.autocrop.activities.main.fragments.flowfield.contracthandlers.OpenDocumentTreeContractHandler
import com.w2sv.autocrop.activities.main.fragments.flowfield.contracthandlers.SelectImagesContractHandlerCompat
import com.w2sv.autocrop.databinding.FlowfieldBinding
import com.w2sv.autocrop.domain.AccumulatedIOResults
import com.w2sv.autocrop.ui.views.animate
import com.w2sv.autocrop.ui.views.fadeIn
import com.w2sv.autocrop.ui.views.fadeInAnimationComposer
import com.w2sv.autocrop.ui.views.fadeOut
import com.w2sv.autocrop.ui.views.onHalfwayFinished
import com.w2sv.autocrop.utils.cropSaveDirPathIdentifier
import com.w2sv.autocrop.utils.extensions.addObservers
import com.w2sv.autocrop.utils.extensions.resolution
import com.w2sv.autocrop.utils.getFragment
import com.w2sv.autocrop.utils.getMediaUri
import com.w2sv.common.BackPressHandler
import com.w2sv.common.PermissionHandler
import com.w2sv.common.preferences.UriRepository
import com.w2sv.cropbundle.io.IMAGE_MIME_TYPE
import com.w2sv.flowfield.Sketch
import com.w2sv.screenshotlistening.ScreenshotListener
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import processing.android.PFragment
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
        val uriRepository: UriRepository,
    ) : androidx.lifecycle.ViewModel() {

        val accumulatedIoResults: AccumulatedIOResults? = savedStateHandle[AccumulatedIOResults.EXTRA]

        var fadedInForegroundOnEntry = false

        /**
         * IO Results Notification
         */

        fun showIOResultsNotificationIfApplicable(
            context: Context
        ) {
            accumulatedIoResults?.let {
                context.showToast(it.getNotificationText())
            }
        }

        /**
         * Misc LiveData
         */

        val hideForegroundElementsLive: LiveData<Boolean> by lazy {
            MutableLiveData(false)
        }

        val cropSaveDirIdentifierLive: LiveData<String> by lazy {
            MutableLiveData(cropSaveDirPathIdentifier(uriRepository.documentUri.value))
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
        callback: (Context?, Intent?) -> Unit
    ) : SelfManagingLocalBroadcastReceiver.Impl(
        broadcastManager,
        IntentFilter(ScreenshotListener.OnCancelledFromNotificationListener.ACTION_NOTIFY_ON_SCREENSHOT_LISTENER_CANCELLED_LISTENERS),
        callback
    )

    private val viewModel by viewModels<ViewModel>()

    private val cancelledSSLFromNotificationListener: CancelledSSLFromNotificationListener by lazy {
        CancelledSSLFromNotificationListener(LocalBroadcastManager.getInstance(requireContext())) { _, _ ->
            viewModel.screenshotListenerCancelledFromNotificationLive.postValue(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addObservers(
            listOf(
                cancelledSSLFromNotificationListener,
                selectImagesContractHandler,
                openDocumentTreeContractHandler,
                writeExternalStoragePermissionHandler
            ) + screenshotListeningPermissionHandlers

        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        childFragmentManager
            .beginTransaction()
            .add(
                binding.flowfieldLayout.id,
                PFragment(Sketch(requireActivity().windowManager.resolution))
            )
            .commitAllowingStateLoss()  // Fixes java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState

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
                        viewModel.showIOResultsNotificationIfApplicable(requireContext())

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
                            "Content provider not supported. Please select a different one",
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
                val text = if (viewModel.uriRepository.setNewUri(treeUri, requireContext().contentResolver)) {
                    viewModel.cropSaveDirIdentifierLive.postValue(cropSaveDirPathIdentifier(viewModel.uriRepository.documentUri.value))
                    SpannableStringBuilder()
                        .append("Crops will be saved to ")
                        .color(requireContext().getColor(R.color.success)) {
                            append(viewModel.cropSaveDirIdentifierLive.value!!)
                        }
                }
                else
                    "Directory didn't change"

                requireContext().showToast(text, Toast.LENGTH_LONG)
            }
        }
    }

    fun onBackPress() {
        binding.drawerLayout.run {
            if (isOpen)
                closeDrawer()
            else
                viewModel.backPressHandler(
                    {
                        requireContext().showToast(resources.getString(R.string.tap_again_to_exit))
                    },
                    {
                        requireActivity().finishAffinity()
                    }
                )
        }
    }
}
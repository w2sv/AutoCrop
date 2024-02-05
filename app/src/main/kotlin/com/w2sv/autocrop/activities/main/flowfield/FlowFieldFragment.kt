package com.w2sv.autocrop.activities.main.flowfield

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.androidutils.coroutines.collectFromFlow
import com.w2sv.androidutils.eventhandling.BackPressHandler
import com.w2sv.androidutils.generic.uris
import com.w2sv.androidutils.lifecycle.addObservers
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.androidutils.services.isServiceRunning
import com.w2sv.androidutils.ui.resources.getLong
import com.w2sv.androidutils.ui.views.hide
import com.w2sv.androidutils.ui.views.show
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.AppFragment
import com.w2sv.autocrop.activities.crop.CropActivity
import com.w2sv.autocrop.activities.main.MainActivity
import com.w2sv.autocrop.activities.main.flowfield.contracthandlers.OpenDocumentTreeContractHandler
import com.w2sv.autocrop.activities.main.flowfield.contracthandlers.SelectImagesContractHandlerCompat
import com.w2sv.autocrop.databinding.FlowfieldBinding
import com.w2sv.autocrop.domain.AccumulatedIOResults
import com.w2sv.autocrop.ui.views.animate
import com.w2sv.autocrop.ui.views.fadeIn
import com.w2sv.autocrop.ui.views.fadeInAnimationComposer
import com.w2sv.autocrop.ui.views.fadeOut
import com.w2sv.autocrop.ui.views.onHalfwayFinished
import com.w2sv.autocrop.utils.cropSaveDirPathIdentifier
import com.w2sv.autocrop.utils.extensions.resolution
import com.w2sv.autocrop.utils.getFragment
import com.w2sv.autocrop.utils.getMediaUri
import com.w2sv.common.Constants
import com.w2sv.common.PermissionHandler
import com.w2sv.cropbundle.io.IMAGE_MIME_TYPE_MEDIA_STORE_IDENTIFIER
import com.w2sv.domain.repository.PreferencesRepository
import com.w2sv.flowfield.Sketch
import com.w2sv.screenshotlistening.ScreenshotListener
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import processing.android.PFragment
import javax.inject.Inject

@AndroidEntryPoint
class FlowFieldFragment :
    AppFragment<FlowfieldBinding>(FlowfieldBinding::class.java) {

    @HiltViewModel
    class ViewModel @Inject constructor(
        savedStateHandle: SavedStateHandle,
        private val preferencesRepository: PreferencesRepository,
        cancelledSSLFromNotification: ScreenshotListener.CancelledFromNotification,
        @ApplicationContext context: Context
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

        val hideForegroundElementsLive: LiveData<Boolean> = MutableLiveData(false)

        val cropSaveDirIdentifierLive = preferencesRepository.cropSaveDirDocumentUri
            .map { cropSaveDirPathIdentifier(it, context) }
            .asLiveData(Dispatchers.Main)

        val screenshotListenerRunning: LiveData<Boolean> get() = _screenshotListenerRunning
        private val _screenshotListenerRunning = MutableLiveData(context.isServiceRunning<ScreenshotListener>())

        fun setScreenshotListenerRunning(value: Boolean) {
            _screenshotListenerRunning.postValue(value)
        }

        init {
            viewModelScope.collectFromFlow(cancelledSSLFromNotification.sharedFlow) {
                setScreenshotListenerRunning(false)
            }
        }

        fun setCropSaveDirTreeUri(treeUri: Uri, contentResolver: ContentResolver) {
            viewModelScope.launch { preferencesRepository.saveCropSaveDirTreeUri(treeUri) }
            contentResolver
                .takePersistableUriPermission(
                    treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
        }

        val cropSaveDirTreeUri = preferencesRepository.cropSaveDirTreeUri

        /**
         * BackPressListener
         */

        val backPressHandler = BackPressHandler(
            coroutineScope = viewModelScope,
            confirmationWindowDuration = Constants.CONFIRMATION_WINDOW_DURATION
        )
    }

    private val viewModel by viewModels<ViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        addObservers(
            listOf(
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
                        .setType(IMAGE_MIME_TYPE_MEDIA_STORE_IDENTIFIER),
                    null
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
            R.string.media_file_writing_required_for_saving_crops,
            R.string.go_to_app_settings_and_grant_media_file_writing_in_order_for_the_app_to_work
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
                            R.string.content_provider_not_supported_please_select_a_different_one,
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
                viewModel.setCropSaveDirTreeUri(treeUri, requireContext().contentResolver)
                // TODO: show toast in reactive manner on viewModel.cropSaveDirIdentifierLive change
                requireContext().showToast(
                    text = SpannableStringBuilder()
                        .append(getString(R.string.crops_will_be_saved_to))
                        .color(requireContext().getColor(R.color.success)) {
                            append(viewModel.cropSaveDirIdentifierLive.value!!)
                        },
                    duration = Toast.LENGTH_LONG
                )
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

    companion object {
        fun getInstance(accumulatedIoResults: AccumulatedIOResults?): FlowFieldFragment =
            getFragment(FlowFieldFragment::class.java, AccumulatedIOResults.EXTRA to accumulatedIoResults)
    }
}
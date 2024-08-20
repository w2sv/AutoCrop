package com.w2sv.autocrop.activities.main.flowfield

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import com.w2sv.androidutils.isServiceRunning
import com.w2sv.androidutils.lifecycle.ActivityCallContractHandler
import com.w2sv.androidutils.lifecycle.toggle
import com.w2sv.androidutils.res.getLong
import com.w2sv.androidutils.uris
import com.w2sv.androidutils.view.hide
import com.w2sv.androidutils.view.show
import com.w2sv.androidutils.widget.showToast
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.AppFragment
import com.w2sv.autocrop.activities.crop.CropActivity
import com.w2sv.autocrop.activities.examination.IOResults
import com.w2sv.autocrop.activities.main.MainActivity
import com.w2sv.autocrop.databinding.FlowfieldBinding
import com.w2sv.autocrop.ui.views.animate
import com.w2sv.autocrop.ui.views.fadeIn
import com.w2sv.autocrop.ui.views.fadeInAnimationComposer
import com.w2sv.autocrop.ui.views.fadeOut
import com.w2sv.autocrop.ui.views.onHalfwayFinished
import com.w2sv.autocrop.utils.cropSaveDirPathIdentifier
import com.w2sv.autocrop.utils.extensions.resolution
import com.w2sv.autocrop.utils.getFragment
import com.w2sv.autocrop.utils.getMediaUri
import com.w2sv.common.AppPermissionHandler
import com.w2sv.cropbundle.io.IMAGE_MIME_TYPE_MEDIA_STORE_IDENTIFIER
import com.w2sv.domain.repository.PermissionRepository
import com.w2sv.domain.repository.PreferencesRepository
import com.w2sv.flowfield.Sketch
import com.w2sv.kotlinutils.coroutines.collectFromFlow
import com.w2sv.kotlinutils.coroutines.mapState
import com.w2sv.screenshotlistening.ScreenshotListener
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import processing.android.PFragment
import javax.inject.Inject

@AndroidEntryPoint
class FlowFieldFragment :
    AppFragment<FlowfieldBinding>(FlowfieldBinding::class.java) {

    @Inject
    lateinit var permissionRepository: PermissionRepository

    @HiltViewModel
    class ViewModel @Inject constructor(
        savedStateHandle: SavedStateHandle,
        private val preferencesRepository: PreferencesRepository,
        cancelledSSLFromNotification: ScreenshotListener.CancelledFromNotification,
        @ApplicationContext context: Context,
        private val resources: Resources
    ) : androidx.lifecycle.ViewModel() {

        val ioResults: IOResults? = savedStateHandle[IOResults.EXTRA]

        var fadedInForegroundOnEntry = false

        /**
         * IO Results Notification
         */

        fun showIOResultsNotificationIfApplicable(
            context: Context
        ) {
            ioResults?.let {
                context.showToast(it.getNotificationText(resources))
            }
        }

        /**
         * Misc LiveData
         */

        val hideForegroundElements: LiveData<Boolean> get() = _hideForegroundElements
        private val _hideForegroundElements = MutableLiveData(false)

        fun toggleHideForegroundElements() {
            _hideForegroundElements.toggle()
        }

        val cropSaveDirIdentifier = preferencesRepository.cropSaveDirDocumentUri
            .mapState { cropSaveDirPathIdentifier(it, context) }
            .asLiveData()

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
            contentResolver
                .takePersistableUriPermission(
                    treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            viewModelScope.launch { preferencesRepository.saveCropSaveDirTreeUri(treeUri) }
        }

        val cropSaveDirTreeUri = preferencesRepository.cropSaveDirTreeUri
    }

    private val viewModel by viewModels<ViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (listOf(
            selectImagesContractHandler,
            openDocumentTreeContractHandler,
            writeExternalStoragePermissionHandler
        ) + screenshotListeningPermissionHandlers)
            .forEach(lifecycle::addObserver)
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
        val anyCropsSaved = viewModel.ioResults?.anyCropsSaved == true

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
                onGranted = selectImagesContractHandler::selectImages,
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
                        .setType(IMAGE_MIME_TYPE_MEDIA_STORE_IDENTIFIER),
                    null
                )
            )
        }
    }

    private fun ViewModel.setLiveDataObservers() {
        hideForegroundElements.observe(viewLifecycleOwner) { hideForeground ->
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
     * ActivityCallContractHandlers
     */

    private val writeExternalStoragePermissionHandler by lazy {
        AppPermissionHandler(
            activity = requireActivity(),
            permission = Manifest.permission.WRITE_EXTERNAL_STORAGE,
            permissionDeniedMessageRes = R.string.media_file_writing_required_for_saving_crops,
            permissionRationalSuppressedMessageRes = R.string.go_to_app_settings_and_grant_media_file_writing_in_order_for_the_app_to_work,
            permissionPreviouslyRequested = permissionRepository.readExternalStoragePermissionRequested.stateIn(
                viewModel.viewModelScope,
                SharingStarted.Eagerly
            ),
            savePermissionPreviouslyRequested = {
                viewModel.viewModelScope.launch {
                    permissionRepository.readExternalStoragePermissionRequested.save(
                        true
                    )
                }
            }
        )
    }

    val screenshotListeningPermissionHandlers by lazy {
        ScreenshotListener.permissionHandlers(
            componentActivity = requireActivity(),
            permissionRepository = permissionRepository,
            scope = viewModel.viewModelScope
        )
    }

    private val selectImagesContractHandler: SelectImagesContractHandlerCompat<*, *> by lazy {
        SelectImagesContractHandlerCompat.getInstance(
            activity = requireActivity(),
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
            callbackFromQ = { imageUris ->
                if (imageUris.isNotEmpty()) {
                    @SuppressLint("NewApi")
                    if (getMediaUri(context = requireContext(), uri = imageUris.first()) == null) {
                        requireContext().showToast(
                            R.string.content_provider_not_supported_please_select_a_different_one,
                            Toast.LENGTH_LONG
                        )
                    }
                    else {
                        // Take persistable read permission for each Uri; Fixes consecutively occasionally occurring permission exception on reading in bitmap
                        imageUris.forEach {
                            requireContext().contentResolver.takePersistableUriPermission(
                                it,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                        }
                        requireContext().startActivity(
                            Intent(
                                requireContext(),
                                CropActivity::class.java
                            )
                                .putParcelableArrayListExtra(
                                    MainActivity.EXTRA_SELECTED_IMAGE_URIS,
                                    ArrayList(imageUris)
                                )
                        )
                    }
                }
            }
        )
    }

    private val openDocumentTreeContractHandler by lazy {
        OpenDocumentTreeContractHandler(
            activity = requireActivity(),
            resultCallback = {
                it?.let { treeUri ->
                    viewModel.setCropSaveDirTreeUri(treeUri, requireContext().contentResolver)
                }
            }
        )
    }

    fun launchCropSaveDirSelection() {
        openDocumentTreeContractHandler.selectDocument(viewModel.cropSaveDirTreeUri.value)
    }

    fun onBackPress() {
        binding.drawerLayout.run {
            if (isOpen)
                closeDrawer()
            else
                requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    companion object {
        fun getInstance(ioResults: IOResults?): FlowFieldFragment =
            getFragment(FlowFieldFragment::class.java, IOResults.EXTRA to ioResults)
    }
}

private class OpenDocumentTreeContractHandler(
    activity: ComponentActivity,
    override val resultCallback: (Uri?) -> Unit
) : ActivityCallContractHandler.Impl<Uri?, Uri?>(
    activity = activity,
    activityResultContract = object : ActivityResultContracts.OpenDocumentTree() {
        override fun createIntent(context: Context, input: Uri?): Intent =
            super.createIntent(context, input)
                .setFlags(
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                            Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
                )
    }
) {
    fun selectDocument(treeUri: Uri?) {
        resultLauncher.launch(treeUri)
    }
}

private sealed interface SelectImagesContractHandlerCompat<I, O> : ActivityCallContractHandler<I, O> {

    companion object {
        fun getInstance(
            activity: ComponentActivity,
            callbackLowerThanQ: (ActivityResult) -> Unit,
            callbackFromQ: (List<Uri>) -> Unit
        ): SelectImagesContractHandlerCompat<*, *> =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                FromQ(activity, callbackFromQ)
            else
                LowerThanQ(activity, callbackLowerThanQ)
    }

    fun selectImages()

    class LowerThanQ(
        activity: ComponentActivity,
        override val resultCallback: (ActivityResult) -> Unit
    ) : ActivityCallContractHandler.Impl<Intent, ActivityResult>(
        activity,
        ActivityResultContracts.StartActivityForResult()
    ),
        SelectImagesContractHandlerCompat<Intent, ActivityResult> {

        override fun selectImages() {
            resultLauncher.launch(
                Intent(Intent.ACTION_PICK).apply {
                    type = IMAGE_MIME_TYPE_MEDIA_STORE_IDENTIFIER
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }
            )
        }
    }

    class FromQ(
        activity: ComponentActivity,
        override val resultCallback: (List<Uri>) -> Unit
    ) : ActivityCallContractHandler.Impl<PickVisualMediaRequest, List<@JvmSuppressWildcards Uri>>(
        activity,
        ActivityResultContracts.PickMultipleVisualMedia()
    ),
        SelectImagesContractHandlerCompat<PickVisualMediaRequest, List<@JvmSuppressWildcards Uri>> {

        override fun selectImages() {
            resultLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }
}
package com.w2sv.autocrop.ui.screen.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.androidutils.lifecycle.ActivityCallContractHandler
import com.w2sv.androidutils.res.getLong
import com.w2sv.androidutils.view.hide
import com.w2sv.androidutils.view.show
import com.w2sv.androidutils.widget.showToast
import com.w2sv.autocrop.AppFragment
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.FlowfieldBinding
import com.w2sv.autocrop.ui.views.animate
import com.w2sv.autocrop.ui.views.fadeIn
import com.w2sv.autocrop.ui.views.fadeInAnimationComposer
import com.w2sv.autocrop.ui.views.fadeOut
import com.w2sv.autocrop.ui.views.onHalfwayFinished
import com.w2sv.autocrop.util.extensions.resolution
import com.w2sv.autocrop.util.getMediaUri
import com.w2sv.common.AppPermissionHandler
import com.w2sv.cropbundle.io.IMAGE_MIME_TYPE_MEDIA_STORE_IDENTIFIER
import com.w2sv.domain.repository.PermissionRepository
import com.w2sv.flowfield.Sketch
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import processing.android.PFragment
import javax.inject.Inject

@AndroidEntryPoint
class HomeScreenFragment :
    AppFragment<FlowfieldBinding>(FlowfieldBinding::class.java) {

    @Inject
    lateinit var permissionRepository: PermissionRepository

    private val viewModel by viewModels<HomeScreenViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        listOf(
            openDocumentTreeContractHandler,
            writeExternalStoragePermissionHandler
        )
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
        val anyCropsSaved = viewModel.cropBundleIoResults?.anyCropsSaved == true

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
                onGranted = ::launchImageSelection,
            )
        }
        shareCropsButton.setOnClickListener {
            startActivity(
                Intent.createChooser(
                    Intent(Intent.ACTION_SEND_MULTIPLE)
                        .putExtra(
                            Intent.EXTRA_STREAM,
                            viewModel.cropBundleIoResults!!.cropUris
                        )
                        .setType(IMAGE_MIME_TYPE_MEDIA_STORE_IDENTIFIER),
                    null
                )
            )
        }
    }

    private fun HomeScreenViewModel.setLiveDataObservers() {
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

    //    val screenshotListeningPermissionHandlers by lazy {
    //        ScreenshotListener.permissionHandlers(
    //            componentActivity = requireActivity(),
    //            permissionRepository = permissionRepository,
    //            scope = viewModel.viewModelScope
    //        )
    //    }

    private fun launchImageSelection() {
        imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
            if (uris.isNotEmpty()) {
                @SuppressLint("NewApi")
                if (getMediaUri(context = requireContext(), uri = uris.first()) == null) {
                    requireContext().showToast(
                        R.string.content_provider_not_supported_please_select_a_different_one,
                        Toast.LENGTH_LONG
                    )
                }
                else {
                    // Take persistable read permission for each Uri; Fixes consecutively occasionally occurring permission exception on reading in bitmap
                    uris.forEach {
                        requireContext().contentResolver.takePersistableUriPermission(
                            it,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    }
                    navigateToCropScreen(uris.toTypedArray())
                }
            }
        }

    private fun navigateToCropScreen(uris: Array<Uri>) {
        navController.navigate(HomeScreenFragmentDirections.navigateToCropScreen(uris))
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
package com.w2sv.autocrop.ui.screen.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.SimpleDrawerListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.widget.showToast
import com.w2sv.autocrop.AppFragment
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.HomeScreenBinding
import com.w2sv.autocrop.ui.util.fadeIn
import com.w2sv.autocrop.ui.util.fadeInAnimationComposer
import com.w2sv.autocrop.ui.util.fadeOut
import com.w2sv.autocrop.ui.util.onHalfwayFinished
import com.w2sv.autocrop.ui.util.resolution
import com.w2sv.autocrop.util.getMediaUri
import com.w2sv.common.AppPermissionHandler
import com.w2sv.domain.repository.PermissionRepository
import com.w2sv.flowfield.PerlinNoiseFlowFieldSketch
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import processing.android.PFragment
import processing.core.PApplet
import javax.inject.Inject

@AndroidEntryPoint
class HomeScreenFragment :
    AppFragment<HomeScreenBinding>(HomeScreenBinding::class.java) {

    @Inject
    lateinit var permissionRepository: PermissionRepository

    private val viewModel by viewModels<HomeScreenViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycle.addObserver(writeExternalStoragePermissionHandler)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            attachSketch(
                canvas = flowfieldLayout,
                sketch = PerlinNoiseFlowFieldSketch(requireActivity().windowManager.resolution)
            )

            drawerLayout.onDrawerSlide(viewModel::setDrawerSlideOffset)

            navigationViewToggleButton.setOnClickListener { drawerLayout.toggleDrawer() }
            imageSelectionButton.setOnClickListener { launchImageSelection() }
            foregroundElementsToggleButton.setOnClickListener { viewModel.toggleFullFlowFieldDisplay() }

            with(viewModel) {
                if (!fadedInForegroundOnEntry) {
                    foregroundLayout
                        .fadeInAnimationComposer(duration = 3500L)
                        .onHalfwayFinished(lifecycleScope) {
                            viewModel.fadedInForegroundOnEntry = true
                            viewModel.cropBundleIoResults?.notificationMessage(resources)?.let {
                                requireContext().showToast(it)
                            }
                        }
                        .play()
                }

                fullFlowFieldDisplay.observe(viewLifecycleOwner) { hideForeground ->
                    if (hideForeground) {
                        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                        foregroundLayoutParent.setOnClickListener { viewModel.toggleFullFlowFieldDisplay() }
                        foregroundLayout.fadeOut()
                    }
                    else {
                        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                        foregroundLayoutParent.setOnClickListener(null)
                        foregroundLayout.fadeIn()
                    }
                }
                drawerSlideOffset.observe(viewLifecycleOwner) {
                    affectDrawerAssociatedViewsOnSlide(it)
                }
            }
        }
    }

    /**
     * ActivityCallContractHandlers
     */

    private val writeExternalStoragePermissionHandler by lazy {  // TODO: what's this even needed for?
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
        writeExternalStoragePermissionHandler.requestPermissionIfRequired(
            onGranted = {
                imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        )
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

    fun launchCropSaveDirSelection() {
        cropSaveDirPicker.launch(viewModel.cropSaveDirTreeUri.value)
    }

    private val cropSaveDirPicker =
        registerForActivityResult(
            object : ActivityResultContracts.OpenDocumentTree() {
                override fun createIntent(context: Context, input: Uri?): Intent =
                    super.createIntent(context, input)
                        .setFlags(
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                                    Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
                        )
            }
        ) { optionalTreeUri ->
            optionalTreeUri?.let { treeUri ->
                viewModel.setCropSaveDirTreeUri(treeUri, requireContext().contentResolver)
            }
        }
}

private fun DrawerLayout.openDrawer() {
    openDrawer(GravityCompat.START)
}

private fun DrawerLayout.closeDrawer() {
    closeDrawer(GravityCompat.START)
}

private fun DrawerLayout.toggleDrawer() {
    if (isOpen)
        closeDrawer()
    else
        openDrawer()
}

private fun DrawerLayout.onDrawerSlide(callback: (Float) -> Unit) {
    addDrawerListener(
        object : SimpleDrawerListener() {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                callback(slideOffset)
            }
        }
    )
}

private fun HomeScreenBinding.affectDrawerAssociatedViewsOnSlide(slideOffset: Float) {
    navigationViewToggleButton.progress = slideOffset

    val associatedButtonAlpha = 1 - slideOffset
    imageSelectionButton.alpha = associatedButtonAlpha
    foregroundElementsToggleButton.alpha = associatedButtonAlpha
}

private fun Fragment.attachSketch(canvas: View, sketch: PApplet) {
    childFragmentManager
        .beginTransaction()
        .add(
            canvas.id,
            PFragment(sketch)
        )
        .commitAllowingStateLoss()  // Fixes java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
}
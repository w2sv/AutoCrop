package com.w2sv.autocrop.activities.main.fragments.flowfield

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.text.color
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.w2sv.androidutils.extensions.getColoredIcon
import com.w2sv.androidutils.extensions.getLong
import com.w2sv.androidutils.extensions.getThemedColor
import com.w2sv.androidutils.extensions.launchDelayed
import com.w2sv.androidutils.extensions.postValue
import com.w2sv.androidutils.extensions.show
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.ApplicationFragment
import com.w2sv.autocrop.activities.crop.CropActivity
import com.w2sv.autocrop.activities.cropexamination.CropExaminationActivity
import com.w2sv.autocrop.activities.main.MainActivity
import com.w2sv.autocrop.activities.main.fragments.flowfield.dialogs.ScreenshotListenerDialog
import com.w2sv.autocrop.activities.main.fragments.flowfield.dialogs.WelcomeDialog
import com.w2sv.autocrop.databinding.FragmentFlowfieldBinding
import com.w2sv.autocrop.preferences.BooleanPreferences
import com.w2sv.autocrop.preferences.UriPreferences
import com.w2sv.autocrop.screenshotlistening.ScreenshotListener
import com.w2sv.autocrop.ui.fadeIn
import com.w2sv.autocrop.utils.documentUriPathIdentifier
import com.w2sv.autocrop.utils.extensions.snackyBuilder
import com.w2sv.kotlinutils.extensions.numericallyInflected
import com.w2sv.permissionhandler.PermissionHandler
import com.w2sv.permissionhandler.requestPermissions
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FlowFieldFragment :
    ApplicationFragment<FragmentFlowfieldBinding>(FragmentFlowfieldBinding::class.java),
    WelcomeDialog.OnProceedListener,
    ScreenshotListenerDialog.OnConfirmedListener {

    @Inject
    lateinit var booleanPreferences: BooleanPreferences

    @Inject
    lateinit var uriPreferences: UriPreferences

    class ViewModel : androidx.lifecycle.ViewModel() {
        var enteredFragment = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycle.addObserver(writeExternalStoragePermissionHandler)
        screenshotListeningPermissions.forEach {
            it?.let {
                lifecycle.addObserver(it)
            }
        }
    }

    private val viewModel by viewModels<ViewModel>()
    private val activityViewModel by activityViewModels<MainActivity.ViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!viewModel.enteredFragment) {
            binding.buttonsLayout.fadeIn(resources.getLong(R.integer.duration_flowfield_buttons_fade_in))

            if (!booleanPreferences.welcomeDialogsShown)
                lifecycleScope.launchDelayed(resources.getLong(R.integer.delay_large)) {
                    WelcomeDialog().show(childFragmentManager)

                    viewModel.enteredFragment = true
                }
            else
                activityViewModel.ioResults?.let {
                    lifecycleScope.launchDelayed(resources.getLong(R.integer.duration_flowfield_buttons_fade_in_halve)) {
                        showIOSynopsisSnackbar(it)

                        viewModel.enteredFragment = true
                    }
                }
                    ?: run { viewModel.enteredFragment = true }
        }
        else
            binding.buttonsLayout.show()
    }

    val writeExternalStoragePermissionHandler by lazy {
        PermissionHandler(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            requireActivity(),
            "Media file writing required for saving crops",
            "Go to app settings and grant media file writing in order for the app to work"
        )
    }

    val screenshotListeningPermissions by lazy {
        listOf(
            PermissionHandler(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    Manifest.permission.READ_MEDIA_IMAGES
                else
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                requireActivity(),
                "Media file access required for listening to screen captures",
                "Go to app settings and grant media file access for screen capture listening to work"
            ),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                PermissionHandler(
                    Manifest.permission.POST_NOTIFICATIONS,
                    requireActivity(),
                    "If you don't allow for the posting of notifications AutoCrop can't inform you about croppable screenshots",
                    "Go to app settings and enable notification posting for screen capture listening to work"
                )
            else
                null
        )
    }

    override fun onProceed() {
        ScreenshotListenerDialog().show(childFragmentManager)
    }

    override fun onConfirmed() {
        screenshotListeningPermissions
            .iterator()
            .requestPermissions(
                onGranted = {
                    ScreenshotListener.startService(requireContext())
                    activityViewModel.liveScreenshotListenerRunning.postValue(true)
                }
            )
    }

    private fun showIOSynopsisSnackbar(ioResults: CropExaminationActivity.Results) {
        ioResults.let {
            with(requireActivity()) {
                if (it.nSavedCrops == 0)
                    snackyBuilder("Discarded all crops")
                else
                    snackyBuilder(
                        SpannableStringBuilder()
                            .apply {
                                append("Saved ${it.nSavedCrops} ${"crop".numericallyInflected(it.nSavedCrops)} to ")
                                color(getThemedColor(R.color.success)) {
                                    append(it.saveDirName)
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
                            }
                    )
                        .setIcon(getColoredIcon(R.drawable.ic_check_24, R.color.success))
            }
        }
            .build()
            .show()
    }

    val imageSelectionIntentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            activityResult.data?.let { intent ->
                intent.clipData?.let { clipData ->
                    startActivity(
                        Intent(
                            requireActivity(),
                            CropActivity::class.java
                        )
                            .putParcelableArrayListExtra(
                                MainActivity.EXTRA_SELECTED_IMAGE_URIS,
                                ArrayList((0 until clipData.itemCount)
                                    .map { clipData.getItemAt(it).uri })
                            )
                    )
                }
            }
        }

    val saveDestinationSelectionIntentLauncher =
        registerForActivityResult(object : ActivityResultContracts.OpenDocumentTree() {
            override fun createIntent(context: Context, input: Uri?): Intent =
                super.createIntent(context, input)
                    .setFlags(
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                                Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
                    )
        }) {
            it?.let { treeUri ->
                if (uriPreferences.treeUri != treeUri) {
                    uriPreferences.treeUri = treeUri

                    requireContext()
                        .contentResolver
                        .takePersistableUriPermission(
                            treeUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                    requireActivity()
                        .snackyBuilder(
                            SpannableStringBuilder()
                                .append("Crops will be saved to ")
                                .color(requireContext().getThemedColor(R.color.success)) {
                                    append(documentUriPathIdentifier(uriPreferences.documentUri!!))
                                }
                        )
                        .build()
                        .show()
                }
            }
        }
}
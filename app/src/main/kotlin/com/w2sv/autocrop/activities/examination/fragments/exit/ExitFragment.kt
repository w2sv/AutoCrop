package com.w2sv.autocrop.activities.examination.fragments.exit

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.androidutils.lifecycle.ActivityCallContractHandler
import com.w2sv.androidutils.ui.animations.SimpleAnimationListener
import com.w2sv.autocrop.activities.AppFragment
import com.w2sv.autocrop.activities.examination.ExaminationActivity
import com.w2sv.autocrop.databinding.CropPagerExitBinding
import com.w2sv.autocrop.ui.views.getAnimationComposer
import com.w2sv.autocrop.utils.extensions.launchAfterShortDelay
import com.w2sv.cropbundle.io.CropBundleIOResult
import com.w2sv.cropbundle.io.ScreenshotDeletionResult
import kotlinx.coroutines.launch

class ExitFragment :
    AppFragment<CropPagerExitBinding>(CropPagerExitBinding::class.java) {

    class ViewModel(val deletionApprovalRequiringCropBundleIOResults: List<CropBundleIOResult>) : androidx.lifecycle.ViewModel() {

        class Factory(private val deletionApprovalRequiringCropBundleIOResults: List<CropBundleIOResult>) : ViewModelProvider.Factory {

            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                ViewModel(deletionApprovalRequiringCropBundleIOResults) as T
        }
    }

    private val activityViewModel by activityViewModels<ExaminationActivity.ViewModel>()

    private val viewModel by viewModels<ViewModel>(factoryProducer = { ViewModel.Factory(activityViewModel.getDeletionApprovalRequiringCropBundleIOResults()) })

    private val deleteRequestIntentContractAdministrator: DeleteRequestIntentContractAdministrator? by lazy {
        when (viewModel.deletionApprovalRequiringCropBundleIOResults.isNotEmpty()) {
            true -> DeleteRequestIntentContractAdministrator(
                requireActivity()
            ) {
                if (it.resultCode == Activity.RESULT_OK) {
                    viewModel.deletionApprovalRequiringCropBundleIOResults.forEach { cropBundleIOResult ->
                        cropBundleIOResult.screenshotDeletionResult = ScreenshotDeletionResult.SuccessfullyDeleted
                    }
                }

                launchAfterShortDelay {  // required for appearing of transition animation, which otherwise is just skipped
                    launchAppIconAnimationAndStartMainActivity()
                }
            }

            false -> null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        deleteRequestIntentContractAdministrator?.let {
            lifecycle.addObserver(it)
        }
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? =
        if (enter)
            AnimationUtils.loadAnimation(requireActivity(), nextAnim)
                .apply {
                    setAnimationListener(
                        object : SimpleAnimationListener() {

                            override fun onAnimationEnd(animation: Animation?) {
                                deleteRequestIntentContractAdministrator?.emitDeleteRequest(
                                    requireContext().contentResolver,
                                    viewModel.deletionApprovalRequiringCropBundleIOResults.map {
                                        (it.screenshotDeletionResult as ScreenshotDeletionResult.DeletionApprovalRequired).requestUri
                                    }
                                )
                                    ?: launchAppIconAnimationAndStartMainActivity()
                            }
                        }
                    )
                }
        else
            super.onCreateAnimation(transit, false, nextAnim)

    private fun launchAppIconAnimationAndStartMainActivity() {
        lifecycleScope.launch {
            binding.deleteRequestLayout.appLogoIv.getAnimationComposer(
                listOf(
                    Techniques.Wobble,
                    Techniques.Wave,
                    Techniques.Tada
                )
                    .random()
            )
                .onEnd {
                    activityViewModel.startMainActivity(requireActivity())
                }
                .play()
        }
    }
}

@SuppressLint("NewApi")
private class DeleteRequestIntentContractAdministrator(
    activity: ComponentActivity,
    override val resultCallback: (ActivityResult) -> Unit
) : ActivityCallContractHandler.Impl<IntentSenderRequest, ActivityResult>(
    activity,
    ActivityResultContracts.StartIntentSenderForResult()
) {
    fun emitDeleteRequest(contentResolver: ContentResolver, deletionRequestUris: Collection<Uri>) {
        resultLauncher.launch(
            IntentSenderRequest.Builder(
                MediaStore.createDeleteRequest(
                    contentResolver,
                    deletionRequestUris
                )
                    .intentSender
            )
                .build()
        )
    }
}
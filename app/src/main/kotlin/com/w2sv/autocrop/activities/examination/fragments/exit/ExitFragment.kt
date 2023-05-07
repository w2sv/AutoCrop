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
import androidx.lifecycle.lifecycleScope
import com.daimajia.androidanimations.library.Techniques
import com.w2sv.androidutils.coroutines.invokeOnCompletion
import com.w2sv.androidutils.lifecycle.ActivityCallContractHandler
import com.w2sv.androidutils.ui.animations.SimpleAnimationListener
import com.w2sv.autocrop.activities.AppFragment
import com.w2sv.autocrop.activities.examination.ExaminationActivity
import com.w2sv.autocrop.databinding.CropPagerExitBinding
import com.w2sv.autocrop.ui.views.getAnimationComposer
import com.w2sv.autocrop.utils.extensions.launchAfterShortDelay
import kotlinx.coroutines.launch

@SuppressLint("NewApi")
class ExitFragment :
    AppFragment<CropPagerExitBinding>(CropPagerExitBinding::class.java) {

    private val activityViewModel by activityViewModels<ExaminationActivity.ViewModel>()

    private var deleteRequestIntentContractAdministrator: DeleteRequestIntentContractAdministrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (activityViewModel.deleteRequestUris.isNotEmpty()) {
            deleteRequestIntentContractAdministrator = DeleteRequestIntentContractAdministrator(
                requireActivity()
            ) {
                if (it.resultCode == Activity.RESULT_OK) {
                    activityViewModel.accumulateDeleteRequestUris()
                }

                launchAfterShortDelay {  // required for appearing of transition animation, which otherwise is just skipped
                    exitWhenIOProcessingFinished()
                }
            }
                .also {
                    lifecycle.addObserver(it)
                }
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
                                    activityViewModel.deleteRequestUris
                                )
                                    ?: exitWhenIOProcessingFinished()
                            }
                        }
                    )
                }
        else
            super.onCreateAnimation(transit, false, nextAnim)

    private fun exitWhenIOProcessingFinished() {
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
                    with(activityViewModel) {
                        cropProcessingCoroutine.invokeOnCompletion {
                            startMainActivity(requireActivity())
                        }
                    }
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
    fun emitDeleteRequest(contentResolver: ContentResolver, deletionInquiryUris: ArrayList<Uri>) {
        resultLauncher.launch(
            IntentSenderRequest.Builder(
                MediaStore.createDeleteRequest(
                    contentResolver,
                    deletionInquiryUris
                )
                    .intentSender
            )
                .build()
        )
    }
}
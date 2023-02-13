@file:SuppressLint("NewApi")

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
import com.w2sv.androidutils.ActivityCallContractHandler
import com.w2sv.androidutils.extensions.invokeOnCompletion
import com.w2sv.androidutils.ui.SimpleAnimationListener
import com.w2sv.autocrop.activities.AppFragment
import com.w2sv.autocrop.activities.examination.ExaminationActivity
import com.w2sv.autocrop.databinding.ExitBinding
import com.w2sv.autocrop.ui.views.animationComposer
import kotlinx.coroutines.launch

class ExitFragment :
    AppFragment<ExitBinding>(ExitBinding::class.java) {

    private val activityViewModel by activityViewModels<ExaminationActivity.ViewModel>()

    private val deleteRequestIntentContractAdministrator by lazy {
        DeleteRequestIntentContractAdministrator(
            requireActivity()
        ) {
            if (it.resultCode == Activity.RESULT_OK)
                activityViewModel.onDeleteRequestUrisDeleted()

            launchAfterShortDelay {  // necessary for showing of transition animation, which otherwise is just skipped
                exitAsSoonAsIOProcessingFinished()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (activityViewModel.deleteRequestUrisPresent)
            lifecycle.addObserver(deleteRequestIntentContractAdministrator)
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? =
        if (enter)
            AnimationUtils.loadAnimation(requireActivity(), nextAnim)
                .apply {
                    setAnimationListener(
                        object : SimpleAnimationListener() {
                            override fun onAnimationEnd(animation: Animation?) {
                                emitDeleteRequestOrExit()
                            }
                        }
                    )
                }
        else
            super.onCreateAnimation(transit, false, nextAnim)

    private fun emitDeleteRequestOrExit() {
        when (activityViewModel.deleteRequestUrisPresent) {
            false -> exitAsSoonAsIOProcessingFinished()
            true -> {
                deleteRequestIntentContractAdministrator.emitDeleteRequest(
                    requireContext().contentResolver,
                    activityViewModel.deleteRequestUris
                )
            }
        }
    }

    private fun exitAsSoonAsIOProcessingFinished() {
        lifecycleScope.launch {
            binding.deleteRequestLayout.appLogoIv.animationComposer(
                listOf(
                    Techniques.Wobble,
                    Techniques.Wave,
                    Techniques.Tada
                )
                    .random()
            )
                .onEnd {
                    activityViewModel.cropProcessingCoroutine.invokeOnCompletion {
                        requireCastActivity<ExaminationActivity>().startMainActivity()
                    }
                }
                .play()
        }
    }
}

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
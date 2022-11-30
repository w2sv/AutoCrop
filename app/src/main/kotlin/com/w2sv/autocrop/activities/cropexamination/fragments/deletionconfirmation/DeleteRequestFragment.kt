package com.w2sv.autocrop.activities.cropexamination.fragments.deletionconfirmation

import android.annotation.SuppressLint
import android.app.Activity
import android.provider.MediaStore
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import com.w2sv.autocrop.activities.ApplicationFragment
import com.w2sv.autocrop.activities.cropexamination.CropExaminationActivity
import com.w2sv.autocrop.activities.cropexamination.fragments.apptitle.AppTitleFragment
import com.w2sv.autocrop.databinding.FragmentDeleteRequestBinding
import com.w2sv.autocrop.utils.AnimationListenerImpl
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("NewApi")
@AndroidEntryPoint
class DeleteRequestFragment :
    ApplicationFragment<FragmentDeleteRequestBinding>(FragmentDeleteRequestBinding::class.java) {

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? =
        if (enter)
            AnimationUtils.loadAnimation(requireActivity(), nextAnim)
                .apply {
                    setAnimationListener(
                        object : AnimationListenerImpl() {
                            override fun onAnimationEnd(animation: Animation?) {
                                emitDeleteRequest()
                            }
                        }
                    )
                }
        else
            super.onCreateAnimation(transit, false, nextAnim)

    private val activityViewModel by activityViewModels<CropExaminationActivity.ViewModel>()

    private fun emitDeleteRequest() {
        deletionConfirmationInquiryContract.launch(
            IntentSenderRequest.Builder(
                MediaStore.createDeleteRequest(
                    requireContext().contentResolver,
                    activityViewModel.deletionInquiryUris
                )
                    .intentSender
            )
                .build()
        )
    }

    private val deletionConfirmationInquiryContract =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            // increment sharedViewModel.nDeletedScreenshots if deletion request emitted
            if (it.resultCode == Activity.RESULT_OK)
                with(activityViewModel) {
                    nDeletedScreenshots += deletionInquiryUris.size
                }

            launchAfterShortDelay {  // necessary for showing of transition animation, which otherwise is just skipped
                getFragmentHostingActivity()
                    .fragmentReplacementTransaction(
                        AppTitleFragment(),
                        true
                    )
                    .commit()
            }
        }
}
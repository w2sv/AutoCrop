package com.w2sv.autocrop.activities.cropexamination.fragments.deletionconfirmation

import android.app.Activity
import android.os.Build
import android.provider.MediaStore
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import com.w2sv.autocrop.activities.ApplicationFragment
import com.w2sv.autocrop.activities.cropexamination.CropExaminationActivity
import com.w2sv.autocrop.activities.cropexamination.fragments.apptitle.AppTitleFragment
import com.w2sv.autocrop.databinding.FragmentDeletionConfirmationInquiryBinding
import com.w2sv.autocrop.utils.AnimationListenerImpl
import dagger.hilt.android.AndroidEntryPoint

@RequiresApi(Build.VERSION_CODES.R)
@AndroidEntryPoint
class DeletionConfirmationInquiryFragment :
    ApplicationFragment<FragmentDeletionConfirmationInquiryBinding>(FragmentDeletionConfirmationInquiryBinding::class.java) {

    private val activityViewModel by activityViewModels<CropExaminationActivity.ViewModel>()

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? =
        if (enter)
            AnimationUtils.loadAnimation(requireActivity(), nextAnim)
                .apply {
                    setAnimationListener(
                        object : AnimationListenerImpl() {
                            override fun onAnimationEnd(animation: Animation?) {
                                launchDeletionConfirmationInquiry()
                            }
                        }
                    )
                }
        else
            super.onCreateAnimation(transit, false, nextAnim)

    private fun launchDeletionConfirmationInquiry() {
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

            getFragmentHostingActivity()
                .fragmentReplacementTransaction(AppTitleFragment(), true)
                .commit()
        }
}
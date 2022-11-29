package com.w2sv.autocrop.activities.cropexamination.fragments.deletionconfirmation

import android.app.Activity
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.w2sv.androidutils.extensions.getLong
import com.w2sv.androidutils.extensions.launchDelayed
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.ApplicationFragment
import com.w2sv.autocrop.activities.cropexamination.CropExaminationActivity
import com.w2sv.autocrop.activities.cropexamination.fragments.apptitle.AppTitleFragment
import com.w2sv.autocrop.databinding.FragmentDeletionQueryBinding

class DeletionConfirmationDialogFragment :
    ApplicationFragment<FragmentDeletionQueryBinding>(FragmentDeletionQueryBinding::class.java) {

    private val activityViewModel by activityViewModels<CropExaminationActivity.ViewModel>()

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onResume() {
        super.onResume()

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

            // launch appTitleFragment after small delay for UX smoothness
            lifecycleScope.launchDelayed(resources.getLong(R.integer.delay_small)) {
                getFragmentHostingActivity().fragmentReplacementTransaction(AppTitleFragment())
                    .commit()
            }
        }
}
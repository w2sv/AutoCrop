package com.w2sv.autocrop.activities.iodetermination.fragments.deletionconfirmation

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.iodetermination.fragments.IODeterminationActivityFragment
import com.w2sv.autocrop.activities.iodetermination.fragments.apptitle.AppTitleFragment
import com.w2sv.autocrop.databinding.FragmentDeletionQueryBinding
import com.w2sv.autocrop.utils.android.extensions.getLong
import com.w2sv.autocrop.utils.android.postDelayed

class DeletionConfirmationDialogFragment :
    IODeterminationActivityFragment<FragmentDeletionQueryBinding>(FragmentDeletionQueryBinding::class.java) {

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        deletionConfirmationInquiryContract.launch(
            IntentSenderRequest.Builder(
                MediaStore.createDeleteRequest(
                    requireContext().contentResolver,
                    sharedViewModel.deletionInquiryUris
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
                with(sharedViewModel) {
                    nDeletedScreenshots += deletionInquiryUris.size
                }

            // launch appTitleFragment after small delay for UX smoothness
            postDelayed(resources.getLong(R.integer.delay_small)) {
                fragmentHostingActivity.fragmentReplacementTransaction(AppTitleFragment())
                    .commit()
            }
        }
}
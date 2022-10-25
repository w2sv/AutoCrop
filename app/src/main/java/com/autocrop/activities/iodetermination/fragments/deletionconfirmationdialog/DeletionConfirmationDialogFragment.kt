package com.autocrop.activities.iodetermination.fragments.deletionconfirmationdialog

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.autocrop.activities.iodetermination.fragments.IODeterminationActivityFragment
import com.autocrop.activities.iodetermination.fragments.apptitle.AppTitleFragment
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.FragmentDeletionQueryBinding

class DeletionConfirmationDialogFragment :
    IODeterminationActivityFragment<FragmentDeletionQueryBinding>(FragmentDeletionQueryBinding::class.java){

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

    private val deletionConfirmationInquiryContract = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()){
        // increment sharedViewModel.nDeletedScreenshots if deletion request emitted
        if (it.resultCode == Activity.RESULT_OK)
            with(sharedViewModel){
                nDeletedScreenshots += deletionInquiryUris.size
            }

        // launch appTitleFragment after small delay for UX smoothness
        Handler(Looper.getMainLooper()).postDelayed({
                fragmentHostingActivity.fragmentReplacementTransaction(AppTitleFragment())
                    .commit()
            },
            resources.getInteger(R.integer.delay_minimal).toLong()
        )
    }
}
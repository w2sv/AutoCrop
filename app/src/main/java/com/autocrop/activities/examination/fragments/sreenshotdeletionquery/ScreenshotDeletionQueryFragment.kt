package com.autocrop.activities.examination.fragments.sreenshotdeletionquery

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
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.autocrop.activities.examination.fragments.apptitle.AppTitleFragment
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.ExaminationFragmentDeletionqueryBinding

class ScreenshotDeletionQueryFragment :
    ExaminationActivityFragment<ExaminationFragmentDeletionqueryBinding>(ExaminationFragmentDeletionqueryBinding::class.java){

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        screenshotDeletionQueryContract.launch(
            IntentSenderRequest.Builder(
                MediaStore.createDeleteRequest(
                    requireContext().contentResolver,
                    sharedViewModel.deletionQueryUris
                )
                    .intentSender
            )
                .build()
        )
    }

    private val screenshotDeletionQueryContract = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()){
        // increment sharedViewModel.nDeletedScreenshots if deletion request emitted
        if (it.resultCode == Activity.RESULT_OK)
            with(sharedViewModel){
                nDeletedScreenshots += deletionQueryUris.size
            }

        // launch appTitleFragment after small delay for UX smoothness
        Handler(Looper.getMainLooper()).postDelayed(
            { fragmentHostingActivity.replaceCurrentFragmentWith(AppTitleFragment()) },
            resources.getInteger(R.integer.delay_minimal).toLong()
        )
    }
}
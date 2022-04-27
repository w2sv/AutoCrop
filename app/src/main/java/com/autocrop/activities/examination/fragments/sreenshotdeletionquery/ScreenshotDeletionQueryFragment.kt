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
import com.w2sv.autocrop.databinding.ExaminationFragmentDeletionqueryBinding

class ScreenshotDeletionQueryFragment :
    ExaminationActivityFragment<ExaminationFragmentDeletionqueryBinding>(){

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val deletionPendingIntent = MediaStore.createDeleteRequest(requireContext().contentResolver, sharedViewModel.deletionQueryScreenshotUris)
        screenshotDeletionQueryContract.launch(IntentSenderRequest.Builder(deletionPendingIntent.intentSender).build())
    }

    private val screenshotDeletionQueryContract = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()){
        // increment sharedViewModel.nDeletedScreenshots if deletion request emitted
        if (it.resultCode == Activity.RESULT_OK)
            sharedViewModel.incrementNDeletedScreenshotsByDeletionQueryScreenshotUris()

        // launch appTitleFragment after small delay for UX smoothness
        Handler(Looper.getMainLooper()).postDelayed(
            {
                with(typedActivity){
                    replaceCurrentFragmentWith(appTitleFragment, true)
                }
            },
            100
        )
    }
}
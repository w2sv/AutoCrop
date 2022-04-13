package com.autocrop.activities.examination.fragments.sreenshotdeletionquery

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.w2sv.autocrop.databinding.ActivityExaminationFragmentDeletionqueryBinding

class ScreenshotDeletionQueryFragment :
    ExaminationActivityFragment<ActivityExaminationFragmentDeletionqueryBinding>(){

    private val screenshotDeletionQueryContract = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()){
        if (it.resultCode == Activity.RESULT_OK)
            sharedViewModel.incrementNDeletedScreenshotsByDeletionQueryScreenshotUris()

        with(typedActivity){
            replaceCurrentFragmentWith(appTitleFragment, false)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val deletionPendingIntent = MediaStore.createDeleteRequest(requireContext().contentResolver, sharedViewModel.deletionQueryScreenshotUris)
        screenshotDeletionQueryContract.launch(IntentSenderRequest.Builder(deletionPendingIntent.intentSender).build())
    }
}
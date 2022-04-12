package com.autocrop.activities.examination.fragments.deletionquery

import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.w2sv.autocrop.databinding.ActivityExaminationFragmentDeletionqueryBinding

class DeletionQueryFragment :
    ExaminationActivityFragment<ActivityExaminationFragmentDeletionqueryBinding>(){

    private val screenshotDeletionQueryContract = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()){
        with(typedActivity){
            replaceCurrentFragmentWith(appTitleFragment, false)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editPendingIntent = MediaStore.createDeleteRequest(requireContext().contentResolver, sharedViewModel.deletionQueryScreenshotUris)
        screenshotDeletionQueryContract.launch(IntentSenderRequest.Builder(editPendingIntent.intentSender).build())
    }
}
package com.autocrop.screencapturelistening.services.cropio

import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.autocrop.screencapturelistening.services.ScreenCaptureListeningService
import com.autocrop.ui.controller.activity.ViewBoundActivity
import com.autocrop.utils.android.extensions.getParcelable

@RequiresApi(Build.VERSION_CODES.R)
class DeleteRequestActivity: ViewBoundActivity(){
    override fun onStart() {
        super.onStart()

        deletionConfirmationInquiryContract.launch(
            IntentSenderRequest.Builder(
                MediaStore.createDeleteRequest(
                    contentResolver,
                    listOf(intent.getParcelable(ScreenCaptureListeningService.DELETE_REQUEST_URI_KEY))
                )
                    .intentSender
            )
                .build()
        )
    }

    private val deletionConfirmationInquiryContract = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()){
        startService(intent.setClass(applicationContext, CropIOService::class.java))
        finishAffinity()
    }
}
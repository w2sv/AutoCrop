package com.autocrop.screencapturelistening.services.cropio

import android.app.Activity
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.autocrop.screencapturelistening.services.main.ScreenCaptureListeningService
import com.autocrop.ui.controller.activity.ViewBoundActivity
import com.autocrop.utils.android.extensions.getParcelable

@RequiresApi(Build.VERSION_CODES.R)
class DeleteRequestActivity: ViewBoundActivity(){
    companion object{
        const val CONFIRMED_DELETION_KEY = "CONFIRMED_DELETION"
    }

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
        startService(
            intent
                .setClass(applicationContext, CropIOService::class.java)
                .putExtra(CONFIRMED_DELETION_KEY, it.resultCode == Activity.RESULT_OK)
        )
        finishAffinity()
    }
}
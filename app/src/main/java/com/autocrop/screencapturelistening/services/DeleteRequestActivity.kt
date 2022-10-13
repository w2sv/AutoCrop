package com.autocrop.screencapturelistening.services

import android.app.Activity
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.autocrop.utils.android.extensions.getParcelable
import com.w2sv.viewboundcontroller.ViewBoundActivity

@RequiresApi(Build.VERSION_CODES.R)
class DeleteRequestActivity: ViewBoundActivity(){
    companion object{
        const val EXTRA_CONFIRMED_DELETION = "com.autocrop.CONFIRMED_DELETION"
    }

    override fun onStart() {
        super.onStart()

        deletionConfirmationInquiryContract.launch(
            IntentSenderRequest.Builder(
                MediaStore.createDeleteRequest(
                    contentResolver,
                    listOf(intent.getParcelable(ScreenCaptureListeningService.EXTRA_DELETE_REQUEST_URI))
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
                .putExtra(EXTRA_CONFIRMED_DELETION, it.resultCode == Activity.RESULT_OK)
        )
        finishAffinity()
    }
}
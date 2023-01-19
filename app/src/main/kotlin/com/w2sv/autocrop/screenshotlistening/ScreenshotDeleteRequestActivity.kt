package com.w2sv.autocrop.screenshotlistening

import android.os.Build
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.w2sv.autocrop.R
import com.w2sv.autocrop.utils.extensions.getParcelableExtraCompat

@RequiresApi(Build.VERSION_CODES.R)
class ScreenshotDeleteRequestActivity : ComponentActivity(R.layout.fragment_delete_request) {
    companion object {
        const val EXTRA_CONFIRMED_DELETION = "com.w2sv.autocrop.extra.CONFIRMED_DELETION"
    }

    override fun onStart() {
        super.onStart()

        deletionConfirmationInquiryContract.launch(
            IntentSenderRequest.Builder(
                MediaStore.createDeleteRequest(
                    contentResolver,
                    listOf(intent.getParcelableExtraCompat(ScreenshotListener.EXTRA_DELETE_REQUEST_URI))
                )
                    .intentSender
            )
                .build()
        )
    }

    private val deletionConfirmationInquiryContract =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            startService(
                intent
                    .setClass(applicationContext, CropIOService::class.java)
                    .putExtra(EXTRA_CONFIRMED_DELETION, it.resultCode == RESULT_OK)
            )
            finishAffinity()
        }
}
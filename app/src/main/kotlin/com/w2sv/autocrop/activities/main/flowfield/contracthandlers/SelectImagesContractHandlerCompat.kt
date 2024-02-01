package com.w2sv.autocrop.activities.main.flowfield.contracthandlers

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.w2sv.androidutils.lifecycle.ActivityCallContractHandler
import com.w2sv.cropbundle.io.IMAGE_MIME_TYPE_MEDIA_STORE_IDENTIFIER

sealed interface SelectImagesContractHandlerCompat<I, O> : ActivityCallContractHandler<I, O> {

    companion object {
        fun getInstance(
            activity: ComponentActivity,
            callbackLowerThanQ: (ActivityResult) -> Unit,
            callbackFromQ: (List<Uri>) -> Unit
        ): SelectImagesContractHandlerCompat<*, *> =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                FromQ(activity, callbackFromQ)
            else
                LowerThanQ(activity, callbackLowerThanQ)
    }

    fun selectImages()

    class LowerThanQ(
        activity: ComponentActivity,
        override val resultCallback: (ActivityResult) -> Unit
    ) : ActivityCallContractHandler.Impl<Intent, ActivityResult>(
        activity,
        ActivityResultContracts.StartActivityForResult()
    ),
        SelectImagesContractHandlerCompat<Intent, ActivityResult> {

        override fun selectImages() {
            resultLauncher.launch(
                Intent(Intent.ACTION_PICK).apply {
                    type = IMAGE_MIME_TYPE_MEDIA_STORE_IDENTIFIER
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }
            )
        }
    }

    class FromQ(
        activity: ComponentActivity,
        override val resultCallback: (List<Uri>) -> Unit
    ) : ActivityCallContractHandler.Impl<PickVisualMediaRequest, List<@JvmSuppressWildcards Uri>>(
        activity,
        ActivityResultContracts.PickMultipleVisualMedia()
    ),
        SelectImagesContractHandlerCompat<PickVisualMediaRequest, List<@JvmSuppressWildcards Uri>> {

        override fun selectImages() {
            resultLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }
}
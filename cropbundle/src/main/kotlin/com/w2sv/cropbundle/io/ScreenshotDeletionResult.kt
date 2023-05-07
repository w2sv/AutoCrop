package com.w2sv.cropbundle.io

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.w2sv.cropbundle.io.extensions.deleteImage

sealed class ScreenshotDeletionResult {
    object None : ScreenshotDeletionResult()
    object SuccessfullyDeleted : ScreenshotDeletionResult()
    object DeletionFailed : ScreenshotDeletionResult()
    class DeletionApprovalRequired(val requestUri: Uri) : ScreenshotDeletionResult()

    companion object {

        fun get(deleteScreenshot: Boolean, mediaStoreId: Long, context: Context): ScreenshotDeletionResult =
            when (deleteScreenshot) {
                true -> when (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    true ->
                        DeletionApprovalRequired(
                            getImageDeleteRequestUri(mediaStoreId)
                        )

                    false -> when (context.contentResolver.deleteImage(mediaStoreId)) {
                        true -> SuccessfullyDeleted
                        false -> DeletionFailed
                    }
                }

                false -> None
            }
    }
}

fun getImageDeleteRequestUri(mediaStoreId: Long): Uri =
    ContentUris.withAppendedId(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        mediaStoreId
    )

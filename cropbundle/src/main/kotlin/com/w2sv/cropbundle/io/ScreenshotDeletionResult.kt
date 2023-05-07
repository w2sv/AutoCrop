package com.w2sv.cropbundle.io

import android.content.Context
import android.net.Uri
import com.w2sv.cropbundle.io.extensions.deleteImage

sealed class ScreenshotDeletionResult {
    object None : ScreenshotDeletionResult()
    object SuccessfullyDeleted : ScreenshotDeletionResult()
    object DeletionFailed : ScreenshotDeletionResult()
    class DeletionApprovalRequired(val requestUri: Uri) : ScreenshotDeletionResult()

    companion object {

        fun get(deleteScreenshot: Boolean, mediaStoreId: Long, context: Context): ScreenshotDeletionResult =
            when (deleteScreenshot) {
                true -> when (IMAGE_DELETION_REQUIRING_APPROVAL) {
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

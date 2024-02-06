package com.w2sv.cropbundle.io

import android.content.ContentResolver
import android.net.Uri
import com.w2sv.cropbundle.io.extensions.deleteImage
import slimber.log.i

sealed interface ScreenshotDeletionResult {
    data object SuccessfullyDeleted : ScreenshotDeletionResult
    data object DeletionFailed : ScreenshotDeletionResult
    data class DeletionApprovalRequired(val requestUri: Uri) : ScreenshotDeletionResult

    companion object {
        fun get(mediaStoreId: Long, contentResolver: ContentResolver): ScreenshotDeletionResult =
            when (IMAGE_DELETION_REQUIRING_APPROVAL) {
                true ->
                    DeletionApprovalRequired(
                        getImageContentUri(mediaStoreId)
                    )

                false -> when (contentResolver.deleteImage(mediaStoreId)) {
                    true -> SuccessfullyDeleted
                    false -> DeletionFailed
                }
            }
                .also {
                    i { "ScreenshotDeletionResult: $it" }
                }
    }
}

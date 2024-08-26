package com.w2sv.cropbundle.io

import android.content.ContentResolver
import android.net.Uri
import android.provider.DocumentsContract
import com.w2sv.cropbundle.Screenshot
import com.w2sv.cropbundle.io.ScreenshotDeletionResult.DeletionFailed
import com.w2sv.cropbundle.io.ScreenshotDeletionResult.SuccessfullyDeleted
import com.w2sv.cropbundle.io.extensions.deleteImage

sealed interface ScreenshotDeletionResult {
    data object SuccessfullyDeleted : ScreenshotDeletionResult
    data object DeletionFailed : ScreenshotDeletionResult
    data class DeletionApprovalRequired(val requestUri: Uri) : ScreenshotDeletionResult

    companion object {
        fun get(screenshot: Screenshot, contentResolver: ContentResolver): ScreenshotDeletionResult {
            return screenshot.mediaStoreData.id?.let { mediaStoreId ->
                when (mediaDeletionRequiresExplicitUserApproval) {
                    true ->
                        DeletionApprovalRequired(
                            getImageContentUri(mediaStoreId)
                        )

                    false -> contentResolver.deleteImage(mediaStoreId).toScreenshotDeletionResult()
                }
            }
                ?: DocumentsContract.deleteDocument(contentResolver, screenshot.uri).toScreenshotDeletionResult()
        }
    }
}

private fun Boolean.toScreenshotDeletionResult() =
    when (this) {
        true -> SuccessfullyDeleted
        false -> DeletionFailed
    }
package com.w2sv.cropping.io

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import com.w2sv.common.util.log
import com.w2sv.cropping.io.extensions.deleteImage
import com.w2sv.domain.model.CropResult
import com.w2sv.domain.model.Screenshot
import com.w2sv.domain.model.ScreenshotDeletionResult
import com.w2sv.domain.model.ScreenshotDeletionResult.DeletionApprovalRequired
import com.w2sv.domain.model.ScreenshotDeletionResult.DeletionFailed
import com.w2sv.domain.model.ScreenshotDeletionResult.SuccessfullyDeleted
import com.w2sv.domain.repository.PreferencesRepository
import javax.inject.Inject

class CropBundleIOProcessingUseCase @Inject constructor(private val preferencesRepository: PreferencesRepository) {
    fun invoke(
        cropBitmap: Bitmap,
        screenshotMediaStoreData: Screenshot.MediaStoreData,
        deleteScreenshot: Boolean,
        context: Context
    ): CropResult =
        CropResult(
            cropFileUri = context.contentResolver.saveBitmap(
                bitmap = cropBitmap,
                mimeType = screenshotMediaStoreData.mimeType,
                fileName = cropFileName(
                    fileName = screenshotMediaStoreData.fileName,
                    mimeType = screenshotMediaStoreData.mimeType
                ),
                parentDocumentUri = preferencesRepository.getWritableCropSaveDirDocumentUriOrNull(context)
            ),
            screenshotDeletionResult = if (deleteScreenshot) {
                screenshotDeletionResult(
                    mediaStoreId = screenshotMediaStoreData.id,
                    contentResolver = context.contentResolver
                )
            }
            else {
                null
            }
        )
}

private fun screenshotDeletionResult(mediaStoreId: Long, contentResolver: ContentResolver): ScreenshotDeletionResult =
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
        .log { "ScreenshotDeletionResult: $it" }

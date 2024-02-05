package com.w2sv.cropbundle.io

import android.content.Context
import android.graphics.Bitmap
import com.w2sv.cropbundle.Screenshot
import com.w2sv.domain.repository.PreferencesRepository
import javax.inject.Inject

class CropBundleIOProcessingUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) {
    fun invoke(
        cropBitmap: Bitmap,
        screenshotMediaStoreData: Screenshot.MediaStoreData,
        deleteScreenshot: Boolean,
        context: Context
    ): CropBundleIOResult =
        CropBundleIOResult(
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
                ScreenshotDeletionResult.get(
                    mediaStoreId = screenshotMediaStoreData.id,
                    contentResolver = context.contentResolver
                )
            }
            else {
                null
            }
        )
}
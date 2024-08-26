package com.w2sv.cropbundle.io

import android.content.Context
import android.graphics.Bitmap
import com.w2sv.cropbundle.Screenshot
import com.w2sv.domain.repository.PreferencesRepository
import slimber.log.i
import javax.inject.Inject

class CropBundleIOProcessingUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) {
    fun invoke(
        cropBitmap: Bitmap,
        screenshot: Screenshot,
        deleteScreenshot: Boolean,
        context: Context
    ): CropBundleIOResult =
        CropBundleIOResult(
            cropFileUri = context.contentResolver.saveBitmap(
                bitmap = cropBitmap,
                mimeType = screenshot.mediaStoreData.mimeType,
                fileName = cropFileName(
                    fileName = screenshot.mediaStoreData.fileName,
                    mimeType = screenshot.mediaStoreData.mimeType
                ),
                parentDocumentUri = preferencesRepository.getWritableCropSaveDirDocumentUriOrNull(context)
            ),
            screenshotDeletionResult = if (deleteScreenshot) {
                ScreenshotDeletionResult.get(
                    screenshot = screenshot,
                    contentResolver = context.contentResolver
                )
                    .also { i { "Screenshot deletion result: $it" } }
            }
            else {
                null
            }
        )
}
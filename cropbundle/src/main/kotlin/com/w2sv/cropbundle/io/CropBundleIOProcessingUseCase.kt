package com.w2sv.cropbundle.io

import android.content.Context
import android.graphics.Bitmap
import com.w2sv.common.datastore.UriRepository
import com.w2sv.cropbundle.Screenshot
import javax.inject.Inject

class CropBundleIOProcessingUseCase @Inject constructor(
    private val uriRepository: UriRepository
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
                parentDocumentUri = uriRepository.getWritableDocumentUriOrNull(context)
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
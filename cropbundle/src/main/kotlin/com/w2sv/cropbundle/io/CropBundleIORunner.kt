package com.w2sv.cropbundle.io

import android.content.Context
import android.graphics.Bitmap
import com.w2sv.common.datastore.Repository
import com.w2sv.common.datastore.UriRepository
import com.w2sv.cropbundle.Screenshot
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

class CropBundleIORunner @Inject constructor(
    private val uriRepository: UriRepository,
    private val repository: Repository
) {

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface CropBundleIOProcessorEntryPoint {
        fun getCropBundleIOProcessor(): CropBundleIORunner
    }

    companion object {
        fun getInstance(context: Context): CropBundleIORunner =
            EntryPointAccessors.fromApplication(
                context,
                CropBundleIOProcessorEntryPoint::class.java
            )
                .getCropBundleIOProcessor()
    }

    fun invoke(
        cropBitmap: Bitmap,
        screenshotMediaStoreData: Screenshot.MediaStoreData,
        context: Context,
        deleteScreenshot: Boolean = repository.deleteScreenshots.value
    ): CropBundleIOResult =
        CropBundleIOResult(
            cropFileUri = context.contentResolver.saveBitmap(
                bitmap = cropBitmap,
                mimeType = screenshotMediaStoreData.mimeType,
                fileName = cropFileName(
                    screenshotMediaStoreData.fileName,
                    screenshotMediaStoreData.mimeType
                ),
                parentDocumentUri = uriRepository.validDocumentUriOrNull(context)
            ),
            screenshotDeletionResult = ScreenshotDeletionResult.get(
                deleteScreenshot = deleteScreenshot,
                mediaStoreId = screenshotMediaStoreData.id,
                context = context
            )
        )
}
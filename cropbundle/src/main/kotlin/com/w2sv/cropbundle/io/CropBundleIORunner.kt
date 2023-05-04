package com.w2sv.cropbundle.io

import android.content.Context
import android.graphics.Bitmap
import com.w2sv.common.preferences.UriRepository
import com.w2sv.cropbundle.CropBundle
import com.w2sv.cropbundle.Screenshot
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

class CropBundleIORunner @Inject constructor(
    private val uriRepository: UriRepository
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

        fun invoke(context: Context, cropBundle: CropBundle, deleteScreenshot: Boolean): CropBundleIOResult =
            invoke(
                context,
                cropBundle.crop.bitmap,
                cropBundle.screenshot.mediaStoreData,
                deleteScreenshot
            )

        fun invoke(
            context: Context,
            cropBitmap: Bitmap,
            screenshotMediaStoreData: Screenshot.MediaStoreData,
            deleteScreenshot: Boolean
        ): CropBundleIOResult =
            getInstance(context).invoke(
                context,
                cropBitmap,
                screenshotMediaStoreData,
                deleteScreenshot
            )
    }

    private fun invoke(
        context: Context,
        cropBitmap: Bitmap,
        screenshotMediaStoreData: Screenshot.MediaStoreData,
        deleteScreenshot: Boolean
    ): CropBundleIOResult =
        CropBundleIOResult(
            context.contentResolver.saveBitmap(
                cropBitmap,
                screenshotMediaStoreData.mimeType,
                cropFileName(
                    screenshotMediaStoreData.fileName,
                    screenshotMediaStoreData.mimeType
                ),
                uriRepository.validDocumentUriOrNull(context)
            ),
            context.contentResolver.deleteScreenshotIfApplicable(
                screenshotMediaStoreData.id,
                deleteScreenshot
            )
        )
}
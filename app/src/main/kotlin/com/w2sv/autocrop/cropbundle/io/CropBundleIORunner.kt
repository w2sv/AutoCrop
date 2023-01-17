package com.w2sv.autocrop.cropbundle.io

import android.content.Context
import android.graphics.Bitmap
import com.w2sv.autocrop.cropbundle.Screenshot
import com.w2sv.autocrop.preferences.CropSaveDirPreferences
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

class CropBundleIORunner @Inject constructor() {

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface CropBundleIOProcessorEntryPoint {
        fun cropBundleIOProcessor(): CropBundleIORunner
    }

    companion object {
        fun getInstance(context: Context): CropBundleIORunner {
            val hiltEntryPoint = EntryPointAccessors.fromApplication(
                context,
                CropBundleIOProcessorEntryPoint::class.java
            )
            return hiltEntryPoint.cropBundleIOProcessor()
        }
    }

    @Inject
    lateinit var cropSaveDirPreferences: CropSaveDirPreferences

    @Inject
    @ApplicationContext
    lateinit var context: Context

    fun invoke(
        cropBitmap: Bitmap,
        screenshotMediaStoreData: Screenshot.MediaStoreData,
        deleteScreenshot: Boolean
    ): CropBundleIOResult =
        CropBundleIOResult(
            context.contentResolver.saveBitmap(
                cropBitmap,
                screenshotMediaStoreData.parsedMimeType,
                cropFileName(
                    screenshotMediaStoreData.fileName,
                    screenshotMediaStoreData.parsedMimeType
                ),
                cropSaveDirPreferences.validDocumentUriOrNull(context)
            ),
            context.contentResolver.deleteScreenshotIfApplicable(
                screenshotMediaStoreData.id,
                deleteScreenshot
            )
        )
}
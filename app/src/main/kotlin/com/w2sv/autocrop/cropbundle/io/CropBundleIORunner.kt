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
        fun getInstance(appContext: Context): CropBundleIORunner {
            val hiltEntryPoint = EntryPointAccessors.fromApplication(
                appContext,
                CropBundleIOProcessorEntryPoint::class.java
            )
            return hiltEntryPoint.cropBundleIOProcessor()
        }
    }

    @Inject
    lateinit var cropSaveDirPreferences: CropSaveDirPreferences

    @Inject
    @ApplicationContext
    lateinit var applicationContext: Context

    fun invoke(
        cropBitmap: Bitmap,
        screenshotMediaStoreData: Screenshot.MediaStoreData,
        deleteScreenshot: Boolean
    ): CropBundleIOResult =
        CropBundleIOResult(
            applicationContext.contentResolver.saveBitmap(
                cropBitmap,
                screenshotMediaStoreData.parsedMimeType,
                cropFileName(
                    screenshotMediaStoreData.fileName,
                    screenshotMediaStoreData.parsedMimeType
                ),
                cropSaveDirPreferences.validDocumentUriOrNull(applicationContext)
            ),
            applicationContext.contentResolver.deleteScreenshotIfApplicable(
                screenshotMediaStoreData.id,
                deleteScreenshot
            )
        )
}
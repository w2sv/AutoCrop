package com.w2sv.autocrop.cropbundle.io

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.w2sv.autocrop.cropbundle.Screenshot
import com.w2sv.autocrop.preferences.UriPreferences
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

@EntryPoint
@InstallIn(SingletonComponent::class)
class CropBundleIOProcessor{

    @Inject
    lateinit var uriPreferences: UriPreferences

    @Inject
    @ApplicationContext
    lateinit var applicationContext: Context

    fun invoke(cropBitmap: Bitmap, screenshotMediaStoreData: Screenshot.MediaStoreData, deleteScreenshot: Boolean): IOResult =
        screenshotMediaStoreData.let {
            IOResult(
                applicationContext.contentResolver.saveBitmap(
                    cropBitmap,
                    it.parsedMimeType,
                    cropFileName(it.fileName, it.parsedMimeType),
                    uriPreferences.validDocumentUriOrNull(applicationContext)
                ),
                applicationContext.contentResolver.deleteScreenshotIfApplicable(it.id, deleteScreenshot)
            )
        }
}
/**
 * Saves [cropBitmap] under [cropFileName]
 * Deletes/triggers deletion of respective screenshot depending on [deleteScreenshot]
 */
//fun ContentResolver.carryOutCropIO(
//    cropBitmap: Bitmap,
//    screenshotMediaStoreData: Screenshot.MediaStoreData,
//    validSaveDirDocumentUri: Uri?,
//    deleteScreenshot: Boolean
//): IOResult =
//    screenshotMediaStoreData.let {
//        IOResult(
//            saveBitmap(
//                cropBitmap,
//                it.parsedMimeType,
//                cropFileName(it.fileName, it.parsedMimeType),
//                validSaveDirDocumentUri
//            ),
//            deleteScreenshotIfApplicable(it.id, deleteScreenshot)
//        )
//    }

data class IOResult(val cropWriteUri: Uri?, var deletedScreenshot: Boolean?) {

    val successfullySavedCrop: Boolean
        get() =
            cropWriteUri != null
}
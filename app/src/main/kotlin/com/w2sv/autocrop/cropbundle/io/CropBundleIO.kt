package com.w2sv.autocrop.cropbundle.io

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.w2sv.autocrop.cropbundle.io.extensions.compressToAndCloseStream
import com.w2sv.autocrop.cropbundle.io.extensions.deleteImage
import com.w2sv.autocrop.cropbundle.Screenshot
import com.w2sv.kotlinutils.dateTimeNow
import slimber.log.i
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

fun getDeleteRequestUri(mediaStoreId: Long): Uri? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        ContentUris.withAppendedId(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            mediaStoreId
        )
            .also { i { "Built contentUriWithMediaStoreImagesId: $it" } }
    else
        null

fun pathTail(path: String): String =
    "/${path.split("/").takeLast(2).joinToString("/")}"

data class IOResult(val writeUri: Uri?, var deletedScreenshot: Boolean?) {
    val successfullySavedCrop: Boolean get() =
        writeUri != null
}

/**
 * Saves [CropBundle.crop] under [cropFileName] depending on [CropBundle.screenshot].uri
 * Deletes/triggers deletion of respective screenshot depending on [deleteScreenshot]
 */
fun ContentResolver.carryOutCropIO(
    cropBitmap: Bitmap,
    screenshotMediaStoreData: Screenshot.MediaStoreData,
    validSaveDirDocumentUri: Uri?,
    deleteScreenshot: Boolean
): IOResult =
    screenshotMediaStoreData.run {
        IOResult(
            saveBitmap(
                cropBitmap,
                parsedMimeType,
                cropFileName(fileName, parsedMimeType),
                validSaveDirDocumentUri
            ),
            deleteScreenshotIfApplicable(id, deleteScreenshot)
        )
    }

fun ContentResolver.deleteScreenshotIfApplicable(mediaStoreId: Long, delete: Boolean): Boolean? =
    if (delete)
        deleteImage(mediaStoreId)
    else
        null

fun cropFileName(fileName: String, mimeType: ImageMimeType): String =
    "${fileNameWOExtension(fileName)}-${CROP_FILE_ADDENDUM}_${dateTimeNow()}.${mimeType.fileExtension}"

const val CROP_FILE_ADDENDUM = "AutoCropped"

fun fileNameWOExtension(fileName: String): String =
    fileName.replaceAfterLast(".", "")
        .run { slice(0 until lastIndex) }

//$$$$$$$$$$$$$$
// Crop Saving $
//$$$$$$$$$$$$$$

/**
 * @see
 *      https://stackoverflow.com/a/10124040
 *      https://stackoverflow.com/a/59536115
 */
fun ContentResolver.saveBitmap(
    bitmap: Bitmap,
    mimeType: ImageMimeType,
    fileName: String,
    parentDocumentUri: Uri? = null
): Uri? {
    val (outputStream, writeUri) = GetOutputStream(this, fileName, parentDocumentUri, mimeType)

    val successfullySaved = bitmap.compressToAndCloseStream(outputStream, mimeType.compressFormat)
        .also { i { if (it) "Successfully wrote $fileName" else "Couldn't write $fileName" } }
    return if (successfullySaved) writeUri else null
}

@SuppressLint("Recycle")  // Suppress 'OutputStream should be closed' warning
private object GetOutputStream {
    operator fun invoke(
        contentResolver: ContentResolver,
        fileName: String,
        parentDocumentUri: Uri?,
        mimeType: ImageMimeType
    ): Pair<OutputStream, Uri> =
        when {
            parentDocumentUri != null -> fromParentDocument(fileName, contentResolver, parentDocumentUri, mimeType)
            Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q -> untilQ(fileName)
            else -> @RequiresApi(Build.VERSION_CODES.Q) {
                postQ(fileName, contentResolver, mimeType)
            }
        }

    private fun fromParentDocument(
        fileName: String,
        contentResolver: ContentResolver,
        parentDocumentUri: Uri,
        mimeType: ImageMimeType
    ): Pair<OutputStream, Uri> =
        DocumentsContract.createDocument(
            contentResolver,
            parentDocumentUri,
            mimeType.string,
            fileName
        )!!.run {
            contentResolver.openOutputStream(this)!! to this
        }

    private fun untilQ(fileName: String): Pair<OutputStream, Uri> =
        File(systemPicturesDirectory(), fileName).run {
            FileOutputStream(this) to Uri.fromFile(this)
        }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun postQ(
        fileName: String,
        contentResolver: ContentResolver,
        mimeType: ImageMimeType
    ): Pair<OutputStream, Uri> =
        contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType.string)
            }
        )!!.let { newUri ->
            contentResolver.openOutputStream(newUri)!! to newUri
        }
}

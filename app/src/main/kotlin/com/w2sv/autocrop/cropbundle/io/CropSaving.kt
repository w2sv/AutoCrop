package com.w2sv.autocrop.cropbundle.io

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.w2sv.autocrop.cropbundle.io.extensions.compressToAndCloseStream
import com.w2sv.autocrop.cropbundle.io.utils.systemPicturesDirectory
import slimber.log.i
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

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
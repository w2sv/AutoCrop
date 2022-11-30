package com.w2sv.autocrop.cropbundle.io.extensions

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import slimber.log.i

fun ContentResolver.loadBitmap(uri: Uri): Bitmap =
    BitmapFactory.decodeStream(openInputStream(uri))

/**
 *      https://stackoverflow.com/questions/10716642/android-deleting-an-image?noredirect=1&lq=1
 *      https://developer.android.com/training/data-storage/use-cases#modify-delete-media
 *      https://stackoverflow.com/a/12478822/12083276
 *
 * @return flag indicating whether image was successfully deleted
 */
fun ContentResolver.deleteImage(mediaStoreId: Long): Boolean {
    return try {
        val rowsDeleted = delete(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            "${MediaStore.Images.Media._ID}=?",
            arrayOf(mediaStoreId.toString())
        ) != 0
        i {
            if (rowsDeleted)
                "Successfully deleted image"
            else
                "Could not delete image"
        }
        rowsDeleted
    }
    catch (e: NullPointerException) {
        i(e)
        false
    }
}

/**
 * @see
 *      https://stackoverflow.com/a/16511111/12083276
 */
fun ContentResolver.queryMediaStoreData(
    uri: Uri,
    columns: Array<String>,
    selection: String? = null,
    selectionArgs: Array<String>? = null
): List<String> =
    query(
        uri,
        columns,
        selection,
        selectionArgs,
        null
    )!!.run {
        moveToFirst()
        columns.map { getString(getColumnIndexOrThrow(it)) }
            .also { close() }
    }

fun ContentResolver.queryMediaStoreDatum(
    uri: Uri,
    column: String,
    selection: String? = null,
    selectionArgs: Array<String>? = null
): String =
    queryMediaStoreData(uri, arrayOf(column), selection, selectionArgs).first()
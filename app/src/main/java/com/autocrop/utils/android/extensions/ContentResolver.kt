package com.autocrop.utils.android.extensions

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import timber.log.Timber

fun ContentResolver.openBitmap(uri: Uri): Bitmap =
    BitmapFactory.decodeStream(openInputStream(uri))

/**
 * @see
 *      https://stackoverflow.com/questions/10716642/android-deleting-an-image?noredirect=1&lq=1
 *      https://developer.android.com/training/data-storage/use-cases#modify-delete-media
 *      https://stackoverflow.com/a/12478822/12083276
 *
 * @return flag indicating whether image was successfully deleted
 */
fun ContentResolver.deleteImage(mediaStoreId: Long): Boolean{
    return try{
        val rowsDeleted = delete(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            "${MediaStore.Images.Media._ID}=?",
            arrayOf(mediaStoreId.toString())
        ) != 0
        Timber.i(
            if (rowsDeleted)
                "Successfully deleted image"
            else
                "Could not delete image"
        )
        rowsDeleted
    }
    catch (e: NullPointerException){
        Timber.i(e)
        false
    }
}

/**
 * @see
 *      https://stackoverflow.com/a/16511111/12083276
 */
fun ContentResolver.queryMediaStoreColumns(uri: Uri,
                                           mediaColumns: Array<String>,
                                           selection: String? = null,
                                           selectionArgs: Array<String>? = null): List<String> =
    query(
        uri,
        mediaColumns,
        selection,
        selectionArgs,
        null
    )!!.run {
        moveToFirst()
        mediaColumns.map { getString(getColumnIndexOrThrow(it)) }
            .also { close() }
    }
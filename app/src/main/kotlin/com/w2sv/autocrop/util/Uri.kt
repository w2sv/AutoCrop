package com.w2sv.autocrop.util

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.getSimplePath
import com.w2sv.cropping.io.utils.systemPicturesDirectory

fun cropSaveDirPathIdentifier(documentUri: Uri?, context: Context): String =
    documentUri?.let { getDocumentUriPath(it, context) }
        ?: systemPicturesDirectory().path

/**
 * Returns e.g. "primary:Moved/Screenshots" for [documentUri]="content://com.android.externalstorage.documents/document/primary%3AMoved%2FScreenshots".
 *
 * Does not depend on the file corresponding to [documentUri] being present.
 */
private fun getDocumentUriPath(documentUri: Uri, context: Context): String? =
    DocumentFile.fromSingleUri(context, documentUri)?.getSimplePath(context)

// fun treeUriPath(contentResolver: ContentResolver, treeUri: Uri): DocumentsContract.Path? =
//    DocumentsContract.findDocumentPath(
//        contentResolver,
//        DocumentsContract.buildChildDocumentsUriUsingTree(
//            treeUri,
//            DocumentsContract.getTreeDocumentId(treeUri)
//        )
//    )

/**
 * For launching the crop screen as first app destination and querying images without the photo picker for quicker debugging.
 */
fun ContentResolver.getLatestImageUris(imageCount: Int): List<Uri> {
    val uris = mutableListOf<Uri>()

    val projection = arrayOf(MediaStore.Images.Media._ID)
    val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

    query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        null,
        null,
        sortOrder
    )?.use { cursor ->
        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

        var count = 0
        while (cursor.moveToNext() && count < imageCount) {
            val id = cursor.getLong(idColumn)
            val uri = ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                id
            )
            uris.add(uri)
            count++
        }
    }

    return uris
}

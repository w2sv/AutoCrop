package com.w2sv.autocrop.util

import android.content.Context
import android.net.Uri
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

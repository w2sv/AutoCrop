package com.w2sv.autocrop.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.getSimplePath
import com.w2sv.cropbundle.io.utils.systemPicturesDirectory
import slimber.log.i

@RequiresApi(Build.VERSION_CODES.Q)
fun getMediaUri(context: Context, uri: Uri): Uri? =
    try {
        if (DocumentsContract.isDocumentUri(context, uri)) {
            MediaStore.getMediaUri(context, uri)
                .also {
                    i { "Converted to mediaUri: $it" }
                }
        }
        else
            uri  // TODO: eh?
    }
    catch (e: IllegalArgumentException) {
        null
    }

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

//fun treeUriPath(contentResolver: ContentResolver, treeUri: Uri): DocumentsContract.Path? =
//    DocumentsContract.findDocumentPath(
//        contentResolver,
//        DocumentsContract.buildChildDocumentsUriUsingTree(
//            treeUri,
//            DocumentsContract.getTreeDocumentId(treeUri)
//        )
//    )
package com.w2sv.autocrop.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.w2sv.common.preferences.CropSaveDirPreferences
import com.w2sv.cropbundle.io.utils.systemPicturesDirectory
import slimber.log.i

@RequiresApi(Build.VERSION_CODES.Q)
fun getMediaUri(context: Context, uri: Uri): Uri? =
    try {
        if (DocumentsContract.isDocumentUri(context, uri))
            MediaStore.getMediaUri(context, uri)!!
                .also {
                    i { "Converted to mediaUri: $it" }
                }
        else
            uri
    }
    catch (e: IllegalArgumentException) {
        null
    }

val CropSaveDirPreferences.pathIdentifier: String
    get() = documentUri?.let { documentUriPathIdentifier(it) }
        ?: systemPicturesDirectory().path

private fun documentUriPathIdentifier(documentUri: Uri): String =
    documentUri.pathSegments[1]

fun treeUriPath(contentResolver: ContentResolver, treeUri: Uri): DocumentsContract.Path? =
    DocumentsContract.findDocumentPath(
        contentResolver,
        DocumentsContract.buildChildDocumentsUriUsingTree(
            treeUri,
            DocumentsContract.getTreeDocumentId(treeUri)
        )
    )
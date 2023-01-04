package com.w2sv.autocrop.preferences

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.provider.DocumentsContract
import com.w2sv.androidutils.extensions.uriPermissionGranted
import com.w2sv.autocrop.cropbundle.io.utils.systemPicturesDirectory
import com.w2sv.autocrop.utils.documentUriPathIdentifier
import com.w2sv.typedpreferences.descendants.UriPreferences
import slimber.log.i
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CropSaveDirPreferences @Inject constructor(sharedPreferences: SharedPreferences) : UriPreferences<Uri?>(
    "treeUri" to null,
    "documentUri" to null,
    sharedPreferences = sharedPreferences
) {

    var treeUri: Uri? by this
        private set

    var documentUri: Uri? by this
        private set

    fun setNewUri(treeUri: Uri, contentResolver: ContentResolver): Boolean {
        if (treeUri != this.treeUri) {
            this.treeUri = treeUri
            contentResolver
                .takePersistableUriPermission(
                    treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

            documentUri = DocumentsContract.buildDocumentUriUsingTree(
                treeUri,
                DocumentsContract.getTreeDocumentId(treeUri)
            )

            i { "Set new documentUri: $documentUri" }

            return true
        }
        return false
    }

    val cropSaveDirIdentifier: String
        get() = documentUri?.let { documentUriPathIdentifier(it) }
            ?: systemPicturesDirectory().path

    fun validDocumentUriOrNull(context: Context): Uri? =
        documentUri?.let {
            if (context.uriPermissionGranted(it, Intent.FLAG_GRANT_WRITE_URI_PERMISSION))
                it
            else
                null
        }
}
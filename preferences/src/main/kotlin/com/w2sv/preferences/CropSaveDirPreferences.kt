package com.w2sv.preferences

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.provider.DocumentsContract
import com.w2sv.androidutils.extensions.hasPermission
import com.w2sv.androidutils.typedpreferences.UriPreferences
import slimber.log.i
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CropSaveDirPreferences @Inject constructor(appPreferences: SharedPreferences) : UriPreferences<Uri?>(
    "treeUri" to null,
    "documentUri" to null,
    sharedPreferences = appPreferences
) {

    var treeUri: Uri? by this
        private set

    var documentUri: Uri? by this
        private set

    /**
     * @return true if passed [treeUri] != this.[treeUri]
     */
    fun setNewUri(treeUri: Uri, contentResolver: ContentResolver): Boolean {
        if (treeUri != this.treeUri) {
            this.treeUri = treeUri

            // take persistable read & write permission grants regarding treeUri
            contentResolver
                .takePersistableUriPermission(
                    treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

            // build & set documentUri
            documentUri = DocumentsContract.buildDocumentUriUsingTree(
                treeUri,
                DocumentsContract.getTreeDocumentId(treeUri)
            )

            i { "Set new documentUri: $documentUri" }

            return true
        }
        return false
    }

    /**
     * @return [documentUri] if != null and in possession of [Intent.FLAG_GRANT_WRITE_URI_PERMISSION],
     * otherwise null
     */
    fun validDocumentUriOrNull(context: Context): Uri? =
        documentUri?.let {
            if (it.hasPermission(context, Intent.FLAG_GRANT_WRITE_URI_PERMISSION))
                it
            else
                null
        }
}
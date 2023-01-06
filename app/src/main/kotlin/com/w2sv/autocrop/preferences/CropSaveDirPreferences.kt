package com.w2sv.autocrop.preferences

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import com.w2sv.androidutils.extensions.uriPermissionGranted
import com.w2sv.autocrop.cropbundle.io.utils.systemPicturesDirectory
import com.w2sv.typedpreferences.descendants.UriPreferences
import com.w2sv.typedpreferences.extensions.getAppPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import slimber.log.i
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CropSaveDirPreferences @Inject constructor(@ApplicationContext context: Context) : UriPreferences<Uri?>(
    "treeUri" to null,
    "documentUri" to null,
    sharedPreferences = context.getAppPreferences()
) {

    var treeUri: Uri? by this
        private set

    private var documentUri: Uri? by this

    var pathIdentifier: String = treeUri
        ?.let { treeUriPathIdentifier(context.contentResolver, it) }
        ?: systemPicturesDirectory().path
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

            // set new cropSaveDirIdentifier 
            pathIdentifier = treeUriPathIdentifier(contentResolver, treeUri)

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
            if (context.uriPermissionGranted(it, Intent.FLAG_GRANT_WRITE_URI_PERMISSION))
                it
            else
                null
        }
}

private fun treeUriPathIdentifier(contentResolver: ContentResolver, treeUri: Uri): String =
    DocumentsContract.findDocumentPath(
        contentResolver,
        DocumentsContract.buildChildDocumentsUriUsingTree(
            treeUri,
            DocumentsContract.getTreeDocumentId(treeUri)
        )
    )!!
        .path
        .joinToString("/")
        .also { i{"Built treeUriPathIdentifier: $it"} }
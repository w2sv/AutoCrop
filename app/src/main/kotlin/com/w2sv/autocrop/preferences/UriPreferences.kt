package com.w2sv.autocrop.preferences

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.provider.DocumentsContract
import com.w2sv.androidutils.extensions.uriPermissionGranted
import com.w2sv.autocrop.cropbundle.io.utils.systemPicturesDirectory
import com.w2sv.kotlinutils.delegates.MapObservable
import com.w2sv.typedpreferences.descendants.UriPreferences
import slimber.log.i
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UriPreferences @Inject constructor(sharedPreferences: SharedPreferences) : UriPreferences<Uri?>(
    "treeUri" to null,
    sharedPreferences = sharedPreferences
) {

    /**
     * Inherently build [documentUri]
     */
    var treeUri: Uri? by MapObservable(this) { _, oldValue, newValue ->
        if (newValue != null && oldValue != newValue)
            documentUri = DocumentsContract.buildDocumentUriUsingTree(
                treeUri,
                DocumentsContract.getTreeDocumentId(treeUri)
            )
                .also { i { "Set new documentUri: $it" } }
    }

    val cropDirIdentifier: String
        get() = documentUri?.toString()
            ?: systemPicturesDirectory().path

    var documentUri: Uri? = null

    fun validDocumentUriOrNull(context: Context): Uri? =
        documentUri?.let {
            if (context.uriPermissionGranted(it, Intent.FLAG_GRANT_WRITE_URI_PERMISSION))
                it
            else
                null
        }
}
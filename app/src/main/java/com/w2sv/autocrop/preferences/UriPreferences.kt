package com.w2sv.autocrop.preferences

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.provider.DocumentsContract
import com.w2sv.autocrop.utils.android.extensions.uriPermissionGranted
import com.w2sv.kotlinutils.delegates.mapObserver
import de.paul_woitaschek.slimber.i

object UriPreferences : TypedPreferences<Uri?>(mutableMapOf("treeUri" to null)) {
    /**
     * Inherently builds [documentUri]
     */
    var treeUri: Uri? by mapObserver(map) { _, oldValue, newValue ->
        if (newValue != null && oldValue != newValue)
            _documentUri = DocumentsContract.buildDocumentUriUsingTree(
                treeUri,
                DocumentsContract.getTreeDocumentId(treeUri)
            )
                .also { i { "Set new documentUri: $it" } }
    }

    val documentUri: Uri?
        get() = _documentUri
    private var _documentUri: Uri? = null

    fun validDocumentUri(context: Context): Uri? =
        documentUri?.let {
            if (context.uriPermissionGranted(it, Intent.FLAG_GRANT_WRITE_URI_PERMISSION))
                documentUri
            else
                null
        }

    override fun SharedPreferences.writeValue(key: String, value: Uri?) {
        edit()
            .putString(
                key,
                value?.toString()
            )
            .apply()
    }

    override fun SharedPreferences.getValue(key: String, defaultValue: Uri?): Uri? =
        getString(
            key,
            defaultValue?.toString()
        )
            ?.run { Uri.parse(this) }
}
package com.w2sv.autocrop.preferences

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.provider.DocumentsContract
import com.w2sv.autocrop.utils.android.extensions.uriPermissionGranted
import com.w2sv.kotlinutils.delegates.mapObserver
import slimber.log.i

object UriPreferences : TypedPreferences<Uri?>(mutableMapOf("treeUri" to null)) {
    /**
     * Inherently build [documentUri]
     */
    var treeUri: Uri? by mapObserver(map) { _, oldValue, newValue ->
        if (newValue != null && oldValue != newValue)
            documentUri = DocumentsContract.buildDocumentUriUsingTree(
                treeUri,
                DocumentsContract.getTreeDocumentId(treeUri)
            )
                .also { i { "Set new documentUri: $it" } }
    }

    var documentUri: Uri? = null

    fun validDocumentUriOrNull(context: Context): Uri? =
        documentUri?.let {
            if (context.uriPermissionGranted(it, Intent.FLAG_GRANT_WRITE_URI_PERMISSION))
                it
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
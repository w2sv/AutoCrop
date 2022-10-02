package com.autocrop.preferences

import android.content.SharedPreferences
import android.net.Uri
import com.autocrop.utils.android.buildDocumentUriFromTreeUri
import com.autocrop.utils.kotlin.delegates.mapObserver
import timber.log.Timber

object UriPreferences: Preferences<Uri?>(
    mutableMapOf(
        "treeUri" to null
    )
) {

    /**
     * Inherently build [documentUri] upon setting new [treeUri]
     */
    var treeUri: Uri? by mapObserver(map) { _, oldValue, newValue ->
        if (newValue != null && oldValue != newValue)
            _documentUri = buildDocumentUriFromTreeUri(newValue)
                .also { Timber.i("Set new documentUri: $it") }
    }

    val documentUri: Uri?
        get() = _documentUri
    private var _documentUri: Uri? = null

    override fun SharedPreferences.writeValue(key: String, value: Uri?){
        edit()
            .putString(
                key,
                value?.toString()
            ).apply()
    }
    override fun SharedPreferences.getValue(key: String, defaultValue: Uri?): Uri? =
        getString(
            key,
            defaultValue?.toString()
        )
            ?.run { Uri.parse(this) }
}
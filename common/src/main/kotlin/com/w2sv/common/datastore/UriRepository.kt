package com.w2sv.common.datastore

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.w2sv.androidutils.coroutines.getValueSynchronously
import com.w2sv.androidutils.generic.hasPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import slimber.log.i
import javax.inject.Inject

class UriRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    val treeUri by lazy {
        UriPreference(PreferencesKey.TREE_URI)
    }

    val documentUri by lazy {
        UriPreference(PreferencesKey.DOCUMENT_URI)
    }

    /**
     * @return true if passed [treeUri] != this.[treeUri]
     */
    fun setNewUri(treeUri: Uri, contentResolver: ContentResolver): Boolean {
        if (treeUri != this.treeUri.value) {
            this.treeUri.value = treeUri

            // take persistable read & write permission grants regarding treeUri
            contentResolver
                .takePersistableUriPermission(
                    treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

            // build & set documentUri
            documentUri.value = DocumentsContract.buildDocumentUriUsingTree(
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
        documentUri.value?.let {
            if (it.hasPermission(context, Intent.FLAG_GRANT_WRITE_URI_PERMISSION))
                it
            else
                null
        }

    inner class UriPreference(private val key: Preferences.Key<String>) {

        var value: Uri? = dataStore.data.map {
            it[key]?.let { string ->
                Uri.parse(string)
            }
        }
            .getValueSynchronously()
            set(value) {
                field = value

                scope.launch {
                    dataStore.edit {
                        it[key] = value.toString()
                    }
                }
            }
    }
}
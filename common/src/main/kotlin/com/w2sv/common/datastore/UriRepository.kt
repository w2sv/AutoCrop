package com.w2sv.common.datastore

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.w2sv.androidutils.coroutines.mapState
import com.w2sv.androidutils.datastorage.datastore.preferences.PreferencesDataStoreRepository
import com.w2sv.androidutils.generic.hasPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UriRepository @Inject constructor(
    dataStore: DataStore<Preferences>
) : PreferencesDataStoreRepository(dataStore) {

    private val scope = CoroutineScope(Dispatchers.Main)

    private val treeUriPersisted = getPersistedUri(stringPreferencesKey("treeUri"), null)
    suspend fun saveTreeUri(uri: Uri) {
        treeUriPersisted.save(uri)
    }
    val treeUriStateFlow = treeUriPersisted.stateIn(scope, SharingStarted.WhileSubscribed())

    val documentUri = treeUriStateFlow.mapState {
        DocumentsContract.buildDocumentUriUsingTree(
            it,
            DocumentsContract.getTreeDocumentId(it)
        )
    }

    /**
     * @return [documentUri] if != null and in possession of [Intent.FLAG_GRANT_WRITE_URI_PERMISSION],
     * otherwise null
     */
    fun getWritableDocumentUriOrNull(context: Context): Uri? =
        documentUri.value?.let {
            if (it.hasPermission(context, Intent.FLAG_GRANT_WRITE_URI_PERMISSION))
                it
            else
                null
        }
}
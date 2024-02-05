package com.w2sv.datastore

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.w2sv.androidutils.coroutines.mapState
import com.w2sv.androidutils.datastorage.datastore.preferences.PreferencesDataStoreRepository
import com.w2sv.androidutils.generic.hasPermission
import com.w2sv.domain.model.CropAdjustmentMode
import com.w2sv.domain.repository.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    dataStore: DataStore<Preferences>
) : PreferencesDataStoreRepository(dataStore),
    PreferencesRepository {

    private val scope = CoroutineScope(Dispatchers.Default)

    override val comparisonInstructionsShown =
        getPersistedValue(booleanPreferencesKey("comparisonInstructionsShown"), false)

    override val autoScroll = getPersistedValue(booleanPreferencesKey("autoScroll"), true)

    override val deleteScreenshots = getPersistedValue(booleanPreferencesKey("deleteScreenshots"), false)

    override val edgeCandidateThreshold = getPersistedValue(intPreferencesKey("edgeCandidateThreshold"), 150)

    override val cropAdjustmentMode =
        getPersistedValue(intPreferencesKey("cropAdjustmentMode"), CropAdjustmentMode.Manual)

    private val cropSaveDirTreeUriPersisted = getPersistedUri(stringPreferencesKey("treeUri"), null)

    override suspend fun saveCropSaveDirTreeUri(value: Uri) {
        cropSaveDirTreeUriPersisted.save(value)
    }

    override val cropSaveDirTreeUri: StateFlow<Uri?> =
        cropSaveDirTreeUriPersisted.stateIn(scope, SharingStarted.Eagerly)

    override val cropSaveDirDocumentUri: StateFlow<Uri?> =
        cropSaveDirTreeUri.mapState {
            it?.let { nonNullTreeUri ->
                DocumentsContract.buildDocumentUriUsingTree(
                    nonNullTreeUri,
                    DocumentsContract.getTreeDocumentId(it)
                )
            }
        }

    /**
     * @return [cropSaveDirDocumentUri] if != null and in possession of [Intent.FLAG_GRANT_WRITE_URI_PERMISSION],
     * otherwise null
     */
    override fun getWritableCropSaveDirDocumentUriOrNull(context: Context): Uri? =
        cropSaveDirDocumentUri.value?.let {
            if (it.hasPermission(context, Intent.FLAG_GRANT_WRITE_URI_PERMISSION))
                it
            else
                null
        }
}
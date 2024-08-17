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
import com.w2sv.androidutils.hasPermission
import com.w2sv.datastoreutils.preferences.PreferencesDataStoreRepository
import com.w2sv.domain.model.CropAdjustmentMode
import com.w2sv.domain.repository.PreferencesRepository
import com.w2sv.kotlinutils.coroutines.mapState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    dataStore: DataStore<Preferences>
) : PreferencesDataStoreRepository(dataStore),
    PreferencesRepository {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override val comparisonInstructionsShown =
        dataStoreFlow(booleanPreferencesKey("comparisonInstructionsShown"), false)

    override val autoScroll = dataStoreFlow(booleanPreferencesKey("autoScroll"), true)

    override val deleteScreenshots = dataStoreFlow(booleanPreferencesKey("deleteScreenshots"), false)

    override val cropSensitivity = dataStoreFlow(intPreferencesKey("cropSensitivity"), 5)

    override val cropAdjustmentMode =
        dataStoreFlow(intPreferencesKey("cropAdjustmentMode"), CropAdjustmentMode.Manual)

    private val cropSaveDirTreeUriPersisted = dataStoreUriFlow(stringPreferencesKey("treeUri"), null)

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
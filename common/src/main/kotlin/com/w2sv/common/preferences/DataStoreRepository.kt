package com.w2sv.common.preferences

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.w2sv.androidutils.extensions.hasPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import slimber.log.i
import javax.inject.Inject

class DataStoreRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    val onboardingDone by lazy {
        Preference(booleanPreferencesKey("onboardingDone"), false)
    }

    val comparisonInstructionsShown by lazy {
        Preference(booleanPreferencesKey("comparisonInstructionsShown"), false)
    }

    val autoScroll by lazy {
        Preference(booleanPreferencesKey("autoScroll"), true)
    }

    val deleteScreenshots by lazy {
        Preference(booleanPreferencesKey("deleteScreenshots"), true)
    }

    val edgeCandidateThreshold by lazy {
        Preference(intPreferencesKey("edgeCandidateThreshold"), 150)
    }

    val cropAdjustmentModeOrdinal by lazy {
        Preference(intPreferencesKey("cropAdjustmentMode"), 0)
    }

    inner class Preference<T>(private val key: Preferences.Key<T>, defaultValue: T) {

        var value = dataStore.data.map {
            it[key]
                ?: defaultValue
        }
            .getSynchronously()
            set(value) {
                field = value

                scope.launch {
                    dataStore.edit {
                        it[key] = value
                    }
                }
            }
    }
}

class UriRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
){
    private val scope = CoroutineScope(Dispatchers.Default)

    val treeUri by lazy {
        UriPreference(stringPreferencesKey("treeUri"))
    }

    val documentUri by lazy {
        UriPreference(stringPreferencesKey("documentUri"))
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
            .getSynchronously()
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

private fun <T> Flow<T>.getSynchronously(): T =
    runBlocking {
        first()
    }

package com.w2sv.common.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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
                    save(key, value)
                }
            }

        private suspend fun <T> save(preferencesKey: Preferences.Key<T>, value: T) {
            dataStore.edit {
                it[preferencesKey] = value
            }
        }

        private suspend fun save(
            preferencesKey: Preferences.Key<Int>,
            enum: Enum<*>
        ) {
            dataStore.edit {
                it[preferencesKey] = enum.ordinal
            }
        }
    }
}

private fun <T> Flow<T>.getSynchronously(): T =
    runBlocking {
        first()
    }

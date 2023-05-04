package com.w2sv.common.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.w2sv.androidutils.coroutines.getValueSynchronously
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
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
            .getValueSynchronously()
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
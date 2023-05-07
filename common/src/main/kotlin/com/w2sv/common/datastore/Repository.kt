package com.w2sv.common.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.w2sv.androidutils.coroutines.getValueSynchronously
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class Repository @Inject constructor(
    dataStore: DataStore<Preferences>
) : DataStoreRepository(dataStore) {

    val onboardingDone by lazy {
        Preference(PreferencesKey.ONBOARDING_DONE, false)
    }

    val comparisonInstructionsShown by lazy {
        Preference(PreferencesKey.COMPARISON_INSTRUCTIONS_SHOWN, false)
    }

    val autoScroll by lazy {
        Preference(PreferencesKey.AUTO_SCROLL, true)
    }

    val deleteScreenshots by lazy {
        Preference(PreferencesKey.DELETE_SCREENSHOTS, false)
    }

    val edgeCandidateThreshold by lazy {
        Preference(PreferencesKey.EDGE_CANDIDATE_THRESHOLD, 150)
    }

    val cropAdjustmentModeOrdinal by lazy {
        Preference(PreferencesKey.CROP_ADJUSTMENT_MODE, 0)
    }

    inner class Preference<T>(private val key: Preferences.Key<T>, defaultValue: T) {

        /**
         * Setting leads to value being asynchronously written to [dataStore].
         */
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
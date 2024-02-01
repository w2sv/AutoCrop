package com.w2sv.common.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import com.w2sv.androidutils.datastorage.datastore.preferences.PreferencesDataStoreRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepository @Inject constructor(
    dataStore: DataStore<Preferences>
) : PreferencesDataStoreRepository(dataStore) {

    val comparisonInstructionsShown = getPersistedValue(booleanPreferencesKey("comparisonInstructionsShown"), false)

    val autoScroll = getPersistedValue(booleanPreferencesKey("autoScroll"), true)

    val deleteScreenshots = getPersistedValue(booleanPreferencesKey("deleteScreenshots"), false)

    val edgeCandidateThreshold = getPersistedValue(intPreferencesKey("edgeCandidateThreshold"), 150)

    val cropAdjustmentModeOrdinal = getPersistedValue(intPreferencesKey("cropAdjustmentMode"), 0)
}
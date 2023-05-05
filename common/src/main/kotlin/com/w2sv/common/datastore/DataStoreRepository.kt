package com.w2sv.common.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

abstract class DataStoreRepository(protected val dataStore: DataStore<Preferences>) {
    protected val scope = CoroutineScope(Dispatchers.Default)
}
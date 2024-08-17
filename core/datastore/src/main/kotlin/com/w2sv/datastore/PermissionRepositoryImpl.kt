package com.w2sv.datastore

import android.Manifest
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.w2sv.datastoreutils.datastoreflow.DataStoreFlow
import com.w2sv.datastoreutils.preferences.PreferencesDataStoreRepository
import com.w2sv.domain.repository.PermissionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionRepositoryImpl @Inject constructor(dataStore: DataStore<Preferences>) :
    PreferencesDataStoreRepository(dataStore),
    PermissionRepository {

    override val readMediaImagesPermissionRequested: DataStoreFlow<Boolean>? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            dataStoreFlow(booleanPreferencesKey("PermissionHandler.${Manifest.permission.READ_MEDIA_IMAGES}"), false)
        else
            null

    override val readExternalStoragePermissionRequested: DataStoreFlow<Boolean> =
        dataStoreFlow(booleanPreferencesKey("PermissionHandler.${Manifest.permission.READ_EXTERNAL_STORAGE}"), false)

    override val writeExternalStoragePermissionRequested: DataStoreFlow<Boolean> =
        dataStoreFlow(booleanPreferencesKey("PermissionHandler.${Manifest.permission.WRITE_EXTERNAL_STORAGE}"), false)

    override val postNotificationsPermissionRequested: DataStoreFlow<Boolean>? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            dataStoreFlow(booleanPreferencesKey("PermissionHandler.${Manifest.permission.POST_NOTIFICATIONS}"), false)
        else
            null
}
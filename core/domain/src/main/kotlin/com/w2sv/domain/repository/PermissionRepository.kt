package com.w2sv.domain.repository

import com.w2sv.datastoreutils.datastoreflow.DataStoreFlow

interface PermissionRepository {
    val readMediaImagesPermissionRequested: DataStoreFlow<Boolean>?
    val readExternalStoragePermissionRequested: DataStoreFlow<Boolean>
    val writeExternalStoragePermissionRequested: DataStoreFlow<Boolean>
    val postNotificationsPermissionRequested: DataStoreFlow<Boolean>?
}
package com.w2sv.domain.repository

import android.content.Context
import android.net.Uri
import com.w2sv.datastoreutils.datastoreflow.DataStoreFlow
import com.w2sv.domain.model.CropAdjustmentMode
import kotlinx.coroutines.flow.StateFlow

interface PreferencesRepository {
    val comparisonInstructionsShown: DataStoreFlow<Boolean>
    val autoScroll: DataStoreFlow<Boolean>
    val deleteScreenshots: DataStoreFlow<Boolean>
    val cropSensitivity: DataStoreFlow<Int>
    val cropAdjustmentMode: DataStoreFlow<CropAdjustmentMode>

    val cropSaveDirTreeUri: StateFlow<Uri?>
    val cropSaveDirDocumentUri: StateFlow<Uri?>

    suspend fun saveCropSaveDirTreeUri(value: Uri)
    fun getWritableCropSaveDirDocumentUriOrNull(context: Context): Uri?
}
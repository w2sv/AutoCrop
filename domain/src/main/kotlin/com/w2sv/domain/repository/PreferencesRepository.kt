package com.w2sv.domain.repository

import android.content.Context
import android.net.Uri
import com.w2sv.androidutils.datastorage.datastore.preferences.PersistedValue
import com.w2sv.domain.model.CropAdjustmentMode
import kotlinx.coroutines.flow.StateFlow

interface PreferencesRepository {
    val comparisonInstructionsShown: PersistedValue.UniTyped<Boolean>
    val autoScroll: PersistedValue.UniTyped<Boolean>
    val deleteScreenshots: PersistedValue.UniTyped<Boolean>
    val cropSensitivity: PersistedValue.UniTyped<Int>
    val cropAdjustmentMode: PersistedValue.EnumValued<CropAdjustmentMode>

    val cropSaveDirTreeUri: StateFlow<Uri?>
    val cropSaveDirDocumentUri: StateFlow<Uri?>

    suspend fun saveCropSaveDirTreeUri(value: Uri)
    fun getWritableCropSaveDirDocumentUriOrNull(context: Context): Uri?
}
package com.w2sv.autocrop.activities.iodetermination.fragments.croppager.dialogs

import android.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.w2sv.autocrop.preferences.BooleanPreferences

abstract class AbstractCropDialog : DialogFragment() {
    protected fun AlertDialog.Builder.setDeleteCorrespondingScreenshotsOption(text: String) {
        setMultiChoiceItems(
            arrayOf(text),
            booleanArrayOf(BooleanPreferences.deleteScreenshots)
        ) { _, _, _ ->
            BooleanPreferences.deleteScreenshots = !BooleanPreferences.deleteScreenshots
        }
    }
}
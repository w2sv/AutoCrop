package com.w2sv.autocrop.activities.examination.fragments.croppager.dialogs

import android.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.w2sv.autocrop.preferences.BooleanPreferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class AbstractCropDialog : DialogFragment() {

    @Inject
    lateinit var booleanPreferences: BooleanPreferences

    protected fun AlertDialog.Builder.setDeleteCorrespondingScreenshotsOption(text: String) {
        setMultiChoiceItems(
            arrayOf(text),
            booleanArrayOf(booleanPreferences.deleteScreenshots)
        ) { _, _, _ ->
            booleanPreferences.deleteScreenshots = !booleanPreferences.deleteScreenshots
        }
    }
}
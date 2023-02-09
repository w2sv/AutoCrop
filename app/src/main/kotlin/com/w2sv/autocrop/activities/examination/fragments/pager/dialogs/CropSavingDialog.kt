package com.w2sv.autocrop.activities.examination.fragments.pager.dialogs

import android.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.w2sv.preferences.BooleanPreferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
abstract class CropSavingDialog : DialogFragment() {

    companion object {
        const val EXTRA_SHOW_DISCARD_BUTTON = "com.w2sv.autocrop.extra.SHOW_DISMISS_BUTTON"
    }

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
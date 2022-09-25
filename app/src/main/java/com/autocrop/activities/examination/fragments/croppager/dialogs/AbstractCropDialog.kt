package com.autocrop.activities.examination.fragments.croppager.dialogs

import android.app.AlertDialog
import com.autocrop.preferences.BooleanPreferences
import com.autocrop.ui.elements.ExtendedDialogFragment

abstract class AbstractCropDialog: ExtendedDialogFragment(){
    companion object{
        const val CONFIRMED_BUNDLE_ARG_KEY = "CONFIRMED_BUNDLE_ARG"
    }

    fun AlertDialog.Builder.setDeleteCorrespondingScreenshotsOption(text: String){
        setMultiChoiceItems(
            arrayOf(text),
            booleanArrayOf(BooleanPreferences.deleteScreenshots)){ _, _, _ ->
            BooleanPreferences.deleteScreenshots = !BooleanPreferences.deleteScreenshots
        }
    }

    protected abstract fun setFragmentResult(confirmed: Boolean)
}
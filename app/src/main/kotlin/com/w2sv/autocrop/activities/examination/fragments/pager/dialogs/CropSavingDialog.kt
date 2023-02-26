package com.w2sv.autocrop.activities.examination.fragments.pager.dialogs

import android.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.w2sv.autocrop.activities.examination.fragments.pager.CropPagerFragment

abstract class CropSavingDialog : DialogFragment() {

    companion object {
        const val EXTRA_SHOW_DISCARD_BUTTON = "com.w2sv.autocrop.extra.SHOW_DISMISS_BUTTON"
    }

    private val viewModel by viewModels<CropPagerFragment.ViewModel>({ requireParentFragment() })

    protected fun AlertDialog.Builder.setDeleteCorrespondingScreenshotsOption(text: String) {
        setMultiChoiceItems(
            arrayOf(text),
            booleanArrayOf(viewModel.booleanPreferences.deleteScreenshots)
        ) { _, _, _ ->
            viewModel.booleanPreferences.deleteScreenshots = !viewModel.booleanPreferences.deleteScreenshots
        }
    }
}
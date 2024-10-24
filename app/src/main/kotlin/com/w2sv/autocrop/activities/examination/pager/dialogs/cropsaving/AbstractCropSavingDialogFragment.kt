package com.w2sv.autocrop.activities.examination.pager.dialogs.cropsaving

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.w2sv.autocrop.activities.examination.ExaminationActivity
import com.w2sv.autocrop.activities.examination.pager.CropPagerFragment
import com.w2sv.autocrop.ui.views.RoundedDialogFragment

abstract class AbstractCropSavingDialogFragment : RoundedDialogFragment() {

    companion object {
        const val EXTRA_SHOW_DISCARD_BUTTON = "com.w2sv.autocrop.extra.SHOW_DISMISS_BUTTON"
    }

    private val viewModel by activityViewModels<ExaminationActivity.ViewModel>()

    protected fun AlertDialog.Builder.setDeleteCorrespondingScreenshotsOption(text: String) {
        setMultiChoiceItems(
            arrayOf(text),
            booleanArrayOf(viewModel.deleteScreenshots.value)
        ) { _, _, _ ->
            viewModel.toggleDeleteScreenshots()
        }
    }
}
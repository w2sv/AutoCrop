package com.w2sv.autocrop.ui.screen.pager.dialog.cropsaving

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import com.w2sv.autocrop.ui.screen.pager.CropPagerScreenViewModel
import com.w2sv.autocrop.ui.views.RoundedDialogFragment

abstract class AbstractCropProcedureDialogFragment : RoundedDialogFragment() {

    private val viewModel by viewModels<CropPagerScreenViewModel>({ requireParentFragment() })

    protected fun AlertDialog.Builder.setDeleteCorrespondingScreenshotsOption(text: String) {
        setMultiChoiceItems(
            arrayOf(text),
            booleanArrayOf(viewModel.deleteScreenshots.value)
        ) { _, _, _ ->
            viewModel.toggleDeleteScreenshots()
        }
    }
}
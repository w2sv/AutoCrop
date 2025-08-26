package com.w2sv.autocrop.ui.screen.pager.dialog.cropsaving

import androidx.appcompat.app.AlertDialog
import com.w2sv.autocrop.R
import com.w2sv.autocrop.ui.screen.CropBundleViewModel
import com.w2sv.autocrop.ui.screen.cropNavGraphViewModel
import com.w2sv.core.common.R.string as Strings

class CropsProcedureDialogFragment : AbstractCropProcedureDialogFragment() {

    private val cropViewModel by cropNavGraphViewModel<CropBundleViewModel>()

    override fun AlertDialog.Builder.build(): AlertDialog.Builder =
        apply {
            setTitle(getString(Strings.crops_procedure_dialog_title, cropViewModel.cropBundleCount))
            setIcon(R.drawable.ic_save_24)
            setDeleteCorrespondingScreenshotsOption(getString(Strings.delete_corresponding_screenshots))
            setPositiveButton(getString(Strings.yes)) { _, _ ->
                (parentFragment as ResultListener)
                    .onSaveAllCrops()
            }
            setNegativeButton(getString(Strings.no_discard_all)) { _, _ ->
                (parentFragment as ResultListener)
                    .onDiscardAllCrops()
            }
        }

    interface ResultListener {
        fun onSaveAllCrops()
        fun onDiscardAllCrops()
    }
}

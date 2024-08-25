package com.w2sv.autocrop.ui.screen.pager.dialog.cropsaving

import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import com.w2sv.autocrop.R
import com.w2sv.autocrop.ui.screen.CropBundleViewModel

class CropsProcedureDialogFragment : AbstractCropProcedureDialogFragment() {

    private val cropViewModel by activityViewModels<CropBundleViewModel>()

    override fun AlertDialog.Builder.build(): AlertDialog.Builder =
        apply {
            setTitle("Save ${cropViewModel.cropBundleCount} crops?")
            setIcon(R.drawable.ic_save_24)
            setDeleteCorrespondingScreenshotsOption("Delete corresponding screenshots")
            setPositiveButton(getString(R.string.yes)) { _, _ ->
                (parentFragment as ResultListener)
                    .onSaveAllCrops()
            }
            setNegativeButton("No, discard all") { _, _ ->
                (parentFragment as ResultListener)
                    .onDiscardAllCrops()
            }


        }

    interface ResultListener {
        fun onSaveAllCrops()
        fun onDiscardAllCrops()
    }
}
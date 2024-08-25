package com.w2sv.autocrop.ui.screen.pager.dialog.cropsaving

import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.navArgs
import com.w2sv.autocrop.R

class CropProcedureDialogFragment : AbstractCropProcedureDialogFragment() {

    private val args by navArgs<CropProcedureDialogFragmentArgs>()

    override fun AlertDialog.Builder.build(): AlertDialog.Builder =
        apply {
            setTitle("Save crop?")
            setIcon(R.drawable.ic_save_24)
            setDeleteCorrespondingScreenshotsOption("Delete corresponding screenshot")
            setNegativeButton("No, discard crop") { _, _ ->
                resultListener
                    .onDiscardCrop(args.cropBundleIndex)
            }
            setPositiveButton(getString(R.string.yes)) { _, _ ->
                resultListener
                    .onSaveCrop(args.cropBundleIndex)
            }
        }

    private val resultListener
        get() = requireParentFragment() as ResultListener

    interface ResultListener {
        fun onSaveCrop(dataSetPosition: Int)
        fun onDiscardCrop(dataSetPosition: Int)
    }
}
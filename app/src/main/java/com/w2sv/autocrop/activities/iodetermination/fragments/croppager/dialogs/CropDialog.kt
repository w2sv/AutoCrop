package com.w2sv.autocrop.activities.iodetermination.fragments.croppager.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf

class CropDialog :
    AbstractCropDialog() {

    companion object {
        private const val KEY_DATA_SET_POSITION = "com.w2sv.autocrop.DATA_SET_POSITION"

        fun instance(dataSetPosition: Int): CropDialog =
            CropDialog().apply {
                arguments = bundleOf(KEY_DATA_SET_POSITION to dataSetPosition)
            }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(activity).run {
            setTitle("Save crop?")
            setDeleteCorrespondingScreenshotsOption("Delete corresponding screenshot")
            setNegativeButton("No, discard") { _, _ -> notifyResultListener(false) }
            setPositiveButton("Yes") { _, _ -> notifyResultListener(true) }
            create()
        }

    interface ResultListener {
        fun onResult(confirmed: Boolean, dataSetPosition: Int)
    }

    private fun notifyResultListener(confirmed: Boolean) {
        (requireParentFragment() as ResultListener)
            .onResult(
                confirmed,
                requireArguments()
                    .getInt(KEY_DATA_SET_POSITION)
            )
    }
}
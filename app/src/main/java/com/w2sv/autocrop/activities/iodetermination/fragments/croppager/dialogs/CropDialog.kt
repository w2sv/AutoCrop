package com.w2sv.autocrop.activities.iodetermination.fragments.croppager.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf

class CropDialog :
    AbstractCropDialog() {

    companion object {
        const val REQUEST_KEY_RESULT = "com.w2sv.autocrop.CropDialog_RESULT"
        const val DATA_SET_POSITION_BUNDLE_ARG_KEY = "com.w2sv.autocrop.DATA_SET_POSITION_BUNDLE_ARG"
    }

    private val dataSetPosition: Int by lazy {
        requireArguments().getInt(DATA_SET_POSITION_BUNDLE_ARG_KEY)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(activity).run {
            setTitle("Save crop?")
            setDeleteCorrespondingScreenshotsOption("Delete corresponding screenshot")
            setNegativeButton("No, discard") { _, _ -> setFragmentResult(false) }
            setPositiveButton("Yes") { _, _ -> setFragmentResult(true) }
            create()
        }

    override fun setFragmentResult(confirmed: Boolean) {
        requireActivity()
            .supportFragmentManager
            .setFragmentResult(
                REQUEST_KEY_RESULT,
                bundleOf(
                    DATA_SET_POSITION_BUNDLE_ARG_KEY to dataSetPosition,
                    EXTRA_DIALOG_CONFIRMED to confirmed
                )
            )
    }
}
package com.w2sv.autocrop.activities.iodetermination.fragments.croppager.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf

/**
 * Class accounting for procedure dialog display upon screen click,
 * defining respective procedure effects
 */
class CropDialog :
    AbstractCropDialog() {

    companion object {
        const val RESULT_REQUEST_KEY = "CropDialog_RESULT_REQUEST_KEY"
        const val DATA_SET_POSITION_BUNDLE_ARG_KEY = "DATA_SET_POSITION_BUNDLE_ARG"
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
                RESULT_REQUEST_KEY,
                bundleOf(
                    DATA_SET_POSITION_BUNDLE_ARG_KEY to dataSetPosition,
                    EXTRA_DIALOG_CONFIRMED to confirmed
                )
            )
    }
}
package com.w2sv.autocrop.activities.examination.fragments.croppager.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import com.w2sv.autocrop.activities.getFragmentInstance

class SaveCropDialog : CropSavingDialog() {

    companion object {
        private const val EXTRA_DATA_SET_POSITION = "com.w2sv.autocrop.extra.DATA_SET_POSITION"

        fun getInstance(dataSetPosition: Int): SaveCropDialog =
            getFragmentInstance(SaveCropDialog::class.java, EXTRA_DATA_SET_POSITION to dataSetPosition)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(activity).run {
            setTitle("Save crop?")
            setDeleteCorrespondingScreenshotsOption("Delete corresponding screenshot")
            setNegativeButton("No") { _, _ -> }
            setPositiveButton("Yes") { _, _ ->
                (requireParentFragment() as ResultListener)
                    .onCropDialogResult(
                        requireArguments().getInt(EXTRA_DATA_SET_POSITION)
                    )
            }
            create()
        }

    interface ResultListener {
        fun onCropDialogResult(dataSetPosition: Int)
    }
}
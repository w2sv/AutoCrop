package com.w2sv.autocrop.activities.examination.fragments.croppager.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import com.w2sv.autocrop.activities.getFragmentInstance

class SaveCropDialog : CropSavingDialog() {

    companion object {
        private const val EXTRA_DATA_SET_POSITION = "com.w2sv.autocrop.extra.DATA_SET_POSITION"

        fun getInstance(dataSetPosition: Int, showDismissButton: Boolean): SaveCropDialog =
            getFragmentInstance(
                SaveCropDialog::class.java,
                EXTRA_DATA_SET_POSITION to dataSetPosition,
                EXTRA_SHOW_DISMISS_BUTTON to showDismissButton
            )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .run {
                setTitle("Save crop?")
                setDeleteCorrespondingScreenshotsOption("Delete corresponding screenshot")
                setNegativeButton("No") { _, _ -> }
                setPositiveButton("Yes") { _, _ ->
                    (requireParentFragment() as ResultListener)
                        .onSaveCrop(
                            requireArguments().getInt(EXTRA_DATA_SET_POSITION)
                        )
                }
                if (requireArguments().getBoolean(EXTRA_SHOW_DISMISS_BUTTON))
                    setNeutralButton("No, dismiss crop"){_, _ ->
                        (parentFragment as ResultListener)
                            .onDiscardCrop(
                                requireArguments().getInt(EXTRA_DATA_SET_POSITION)
                            )
                    }
                create()
            }

    interface ResultListener {
        fun onSaveCrop(dataSetPosition: Int)
        fun onDiscardCrop(dataSetPosition: Int)
    }
}
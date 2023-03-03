package com.w2sv.autocrop.activities.examination.fragments.pager.dialogs

import androidx.appcompat.app.AlertDialog
import com.w2sv.autocrop.R
import com.w2sv.autocrop.utils.getFragment

class CropSavingDialogFragment : AbstractCropSavingDialogFragment() {

    companion object {
        private const val EXTRA_DATA_SET_POSITION = "com.w2sv.autocrop.extra.DATA_SET_POSITION"

        fun getInstance(dataSetPosition: Int, showDismissButton: Boolean): CropSavingDialogFragment =
            getFragment(
                CropSavingDialogFragment::class.java,
                EXTRA_DATA_SET_POSITION to dataSetPosition,
                EXTRA_SHOW_DISCARD_BUTTON to showDismissButton
            )
    }

    override fun AlertDialog.Builder.build(): AlertDialog.Builder =
        apply {
            setTitle("Save crop?")
            setIcon(R.drawable.ic_save_24)
            setDeleteCorrespondingScreenshotsOption("Delete corresponding screenshot")
            setNegativeButton(getString(R.string.no)) { _, _ -> }
            setPositiveButton(getString(R.string.yes)) { _, _ ->
                (requireParentFragment() as ResultListener)
                    .onSaveCrop(
                        requireArguments().getInt(EXTRA_DATA_SET_POSITION)
                    )
            }
            if (requireArguments().getBoolean(EXTRA_SHOW_DISCARD_BUTTON))
                setNeutralButton("No, discard crop") { _, _ ->
                    (parentFragment as ResultListener)
                        .onDiscardCrop(
                            requireArguments().getInt(EXTRA_DATA_SET_POSITION)
                        )
                }
        }

    interface ResultListener {
        fun onSaveCrop(dataSetPosition: Int)
        fun onDiscardCrop(dataSetPosition: Int)
    }
}
package com.w2sv.autocrop.activities.examination.pager.dialogs.cropsaving

import androidx.appcompat.app.AlertDialog
import com.w2sv.autocrop.R
import com.w2sv.autocrop.utils.getFragment

class SaveAllCropsDialogFragment : AbstractCropSavingDialogFragment() {

    companion object {
        private const val EXTRA_N_CROPS = "com.w2sv.autocrop.extra.EXTRA_N_CROPS"

        fun getInstance(nCrops: Int, showDismissButton: Boolean): SaveAllCropsDialogFragment =
            getFragment(
                SaveAllCropsDialogFragment::class.java,
                EXTRA_N_CROPS to nCrops,
                EXTRA_SHOW_DISCARD_BUTTON to showDismissButton
            )
    }

    override fun AlertDialog.Builder.build(): AlertDialog.Builder =
        apply {
            setTitle("Save ${requireArguments().getInt(EXTRA_N_CROPS)} crops?")
            setIcon(R.drawable.ic_save_24)
            setDeleteCorrespondingScreenshotsOption("Delete corresponding screenshots")
            setNegativeButton(getString(R.string.no)) { _, _ -> }
            setPositiveButton(getString(R.string.yes)) { _, _ ->
                (parentFragment as ResultListener)
                    .onSaveAllCrops()
            }
            if (requireArguments().getBoolean(EXTRA_SHOW_DISCARD_BUTTON))
                setNeutralButton("No, discard all") { _, _ ->
                    (parentFragment as ResultListener)
                        .onDiscardAllCrops()
                }
        }

    interface ResultListener {
        fun onSaveAllCrops()
        fun onDiscardAllCrops()
    }
}
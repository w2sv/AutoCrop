package com.w2sv.autocrop.activities.examination.fragments.pager.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.core.text.italic
import com.w2sv.autocrop.utils.getFragment

class SaveAllCropsDialog : CropSavingDialog() {

    companion object {
        private const val EXTRA_N_CROPS = "com.w2sv.autocrop.extra.EXTRA_N_CROPS"

        fun getInstance(nCrops: Int, showDismissButton: Boolean): SaveAllCropsDialog =
            getFragment(
                SaveAllCropsDialog::class.java,
                EXTRA_N_CROPS to nCrops,
                EXTRA_SHOW_DISCARD_BUTTON to showDismissButton
            )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .apply {
                setTitle(
                    SpannableStringBuilder()
                        .append("Save ")
                        .italic {
                            append("${requireArguments().getInt(EXTRA_N_CROPS)} ")
                        }
                        .append("crops?")
                )
                setDeleteCorrespondingScreenshotsOption("Delete corresponding screenshots")
                setNegativeButton("No") { _, _ -> }
                setPositiveButton("Yes") { _, _ ->
                    (parentFragment as ResultListener)
                        .onSaveAllCrops()
                }
                if (requireArguments().getBoolean(EXTRA_SHOW_DISCARD_BUTTON))
                    setNeutralButton("No, discard all") { _, _ ->
                        (parentFragment as ResultListener)
                            .onDiscardAllCrops()
                    }
            }
            .create()

    interface ResultListener {
        fun onSaveAllCrops()
        fun onDiscardAllCrops()
    }
}
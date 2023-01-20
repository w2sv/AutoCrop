package com.w2sv.autocrop.activities.examination.fragments.croppager.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.core.text.italic
import com.w2sv.autocrop.activities.getFragmentInstance

class SaveAllCropsDialog : CropSavingDialog() {

    companion object{
        private const val EXTRA_N_CROPS = "com.w2sv.autocrop.extra.EXTRA_N_CROPS"

        fun getInstance(nCrops: Int): SaveAllCropsDialog =
            getFragmentInstance(SaveAllCropsDialog::class.java, EXTRA_N_CROPS to nCrops)
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
                        .onCropEntiretyDialogResult()
                }
            }
            .create()

    interface ResultListener {
        fun onCropEntiretyDialogResult()
    }
}
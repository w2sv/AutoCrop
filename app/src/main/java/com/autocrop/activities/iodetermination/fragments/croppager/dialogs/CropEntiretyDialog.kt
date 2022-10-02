package com.autocrop.activities.iodetermination.fragments.croppager.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.core.os.bundleOf
import androidx.core.text.italic

class CropEntiretyDialog: AbstractCropDialog(){
    companion object{
        const val RESULT_REQUEST_KEY = "CROP_ENTIRETY_DIALOG_RESULT_REQUEST_KEY"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .apply {
                setTitle(
                    SpannableStringBuilder()
                        .append("Save ")
                        .italic { append("all ") }
                        .append("crops?")
                )
                setDeleteCorrespondingScreenshotsOption("Delete corresponding screenshots")
                setNegativeButton("No, discard all") { _, _ -> setFragmentResult(false)}
                setPositiveButton("Yes") { _, _ -> setFragmentResult(true)}
            }
            .create()

    override fun setFragmentResult(confirmed: Boolean){
        requireActivity()
            .supportFragmentManager
            .setFragmentResult(
                RESULT_REQUEST_KEY,
                bundleOf(CONFIRMED_BUNDLE_ARG_KEY to confirmed)
            )
    }
}
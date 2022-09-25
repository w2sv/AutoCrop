package com.autocrop.activities.examination.fragments.croppager.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf

class CropEntiretyDialog: AbstractCropDialog(){
    companion object{
        const val RESULT_REQUEST_KEY = "CropEntiretyDialog_RESULT_REQUEST_KEY"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .apply {
                setTitle("Save all crops?")
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
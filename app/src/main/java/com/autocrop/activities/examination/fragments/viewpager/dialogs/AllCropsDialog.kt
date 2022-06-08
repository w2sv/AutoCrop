package com.autocrop.activities.examination.fragments.viewpager.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import com.autocrop.global.BooleanPreferences
import com.autocrop.uielements.ExtendedDialogFragment

class AllCropsDialog
    : ExtendedDialogFragment(){

    companion object{
        const val SAVE_ALL_FRAGMENT_RESULT_KEY = "SAVE_ALL_FRAGMENT_RESULT_KEY"
        const val DISCARD_ALL_FRAGMENT_RESULT_KEY = "DISCARD_ALL_FRAGMENT_RESULT_KEY"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .apply {
                setTitle("Save all crops?")

                setMultiChoiceItems(
                    arrayOf("Delete corresponding screenshots"),
                    booleanArrayOf(BooleanPreferences.deleteScreenshots)){ _, _, _ ->
                        BooleanPreferences.deleteScreenshots = !BooleanPreferences.deleteScreenshots
                }

                setNegativeButton("No, discard all") { _, _ -> setFragmentResult(DISCARD_ALL_FRAGMENT_RESULT_KEY)}
                setPositiveButton("Yes") { _, _ -> setFragmentResult(SAVE_ALL_FRAGMENT_RESULT_KEY)}
            }
            .create()

    private fun setFragmentResult(key: String){
        requireActivity()
            .supportFragmentManager
            .setFragmentResult(key, Bundle.EMPTY)
    }
}
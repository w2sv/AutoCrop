package com.autocrop.activities.examination.fragments.viewpager

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import com.autocrop.global.BooleanUserPreferences
import com.autocrop.uielements.ExtendedDialogFragment

abstract class AllCropsRegardingDialogFragment
    : ExtendedDialogFragment(){

    val resultKey: String = "${this::class.java.name}_CONFIRMED"

    protected fun setFragmentResult() =
        requireActivity()
            .supportFragmentManager
            .setFragmentResult(resultKey, Bundle.EMPTY)
}

class SaveAllConfirmationDialogFragment
    : AllCropsRegardingDialogFragment(){

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .run {
                setTitle("Save all crops?")
                setMultiChoiceItems(arrayOf("Delete corresponding screenshots"), booleanArrayOf(
                    BooleanUserPreferences.deleteScreenshots)){ _, _, _ ->
                    BooleanUserPreferences.toggle(
                        BooleanUserPreferences.Keys.DELETE_SCREENSHOTS
                    )
                }

                setNegativeButton("No") { _, _ -> }
                setPositiveButton("Yes") { _, _ -> setFragmentResult()}
            }
            .create()
}

class DiscardAllConfirmationDialogFragment
    : AllCropsRegardingDialogFragment(){

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .run {
                setTitle("Discard all crops?")
                setNegativeButton("No") { _, _ -> }
                setPositiveButton("Yes") { _, _ -> setFragmentResult() }
            }
            .create()
}
package com.autocrop.activities.examination.fragments.viewpager.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import com.autocrop.global.BooleanUserPreferences
import com.autocrop.uielements.ExtendedDialogFragment

abstract class CropEntiretyProcedureDialog
    : ExtendedDialogFragment(){

    val resultKey: String = "${this::class.java.name}_CONFIRMED"

    protected fun setFragmentResult() =
        requireActivity()
            .supportFragmentManager
            .setFragmentResult(resultKey, Bundle.EMPTY)
}

class SaveAllConfirmationDialog
    : CropEntiretyProcedureDialog(){

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .run {
                setTitle("Save all crops?")
                setMultiChoiceItems(arrayOf("Delete corresponding screenshots"), booleanArrayOf(
                    BooleanUserPreferences.deleteScreenshots)){ _, _, _ ->
                    BooleanUserPreferences.deleteScreenshots = !BooleanUserPreferences.deleteScreenshots
                }

                setNegativeButton("No") { _, _ -> }
                setPositiveButton("Yes") { _, _ -> setFragmentResult()}
            }
            .create()
}

class DiscardAllConfirmationDialog
    : CropEntiretyProcedureDialog(){

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .run {
                setTitle("Discard all crops?")
                setNegativeButton("No") { _, _ -> }
                setPositiveButton("Yes") { _, _ -> setFragmentResult() }
            }
            .create()
}
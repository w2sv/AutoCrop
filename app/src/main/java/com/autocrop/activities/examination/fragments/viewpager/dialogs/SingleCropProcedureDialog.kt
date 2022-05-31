package com.autocrop.activities.examination.fragments.viewpager.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.autocrop.activities.examination.ExaminationActivityViewModel
import com.autocrop.global.BooleanPreferences
import com.autocrop.uielements.ExtendedDialogFragment
import com.autocrop.utils.executeAsyncTask

/**
 * Class accounting for procedure dialog display upon screen click,
 * defining respective procedure effects
 */
class SingleCropProcedureDialog
        : ExtendedDialogFragment() {

    private val sharedViewModel: ExaminationActivityViewModel by activityViewModels()

    companion object{
        const val DATA_SET_POSITION_IN = "DATA_SET_POSITION_IN"
        const val DATA_SET_POSITION_OUT = "DATA_SET_POSITION_OUT"

        const val PROCEDURE_SELECTED = "ON_PROCEDURE_SELECTED"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(activity).run {
            setTitle("Save crop?")

            setMultiChoiceItems(arrayOf("Delete corresponding screenshot"), booleanArrayOf(
                BooleanPreferences.deleteScreenshots)
            ){ _, _, _ ->
                BooleanPreferences.deleteScreenshots = !BooleanPreferences.deleteScreenshots
            }

            val dataSetPosition = requireArguments().getInt(DATA_SET_POSITION_IN)

            setNegativeButton("No, discard") { _, _ -> triggerOnProcedureSelected(dataSetPosition) }
            setPositiveButton("Yes") { _, _ ->
                sharedViewModel.singleCropSavingJob = lifecycleScope.executeAsyncTask(
                    {
                        saveCrop(
                            dataSetPosition,
                            BooleanPreferences.deleteScreenshots
                        )
                    }
                )
                triggerOnProcedureSelected(dataSetPosition)
            }

            create()
        }

    private fun saveCrop(dataSetPosition: Int, deleteScreenshot: Boolean) =
        sharedViewModel.processCropBundle(dataSetPosition, deleteScreenshot, requireContext())

    private fun triggerOnProcedureSelected(dataSetPosition: Int) =
        requireActivity().supportFragmentManager.setFragmentResult(
            PROCEDURE_SELECTED,
            bundleOf(DATA_SET_POSITION_OUT to dataSetPosition)
        )
}
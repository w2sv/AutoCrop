package com.autocrop.activities.examination.fragments.viewpager

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.ExaminationActivityViewModel
import com.autocrop.global.BooleanUserPreferences
import com.autocrop.uielements.ExtendedDialogFragment
import com.autocrop.utils.executeAsyncTask
import kotlinx.coroutines.Job

/**
 * Class accounting for procedure dialog display upon screen click,
 * defining respective procedure effects
 */
class CropProcedureDialog
        : ExtendedDialogFragment() {

    companion object{
        const val DATA_SET_POSITION_IN = "DATA_SET_POSITION_IN"
        const val DATA_SET_POSITION_OUT = "DATA_SET_POSITION_OUT"
        const val PROCEDURE_SELECTED = "ON_PROCEDURE_SELECTED"
    }

    var cropBundleProcessingJob: Job? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(activity).run {
            setTitle("Save crop?")

            setMultiChoiceItems(arrayOf("Delete corresponding screenshot"), booleanArrayOf(
                BooleanUserPreferences.deleteIndividualScreenshot)){ _, _, _ -> BooleanUserPreferences.toggle(
                BooleanUserPreferences.Keys.deleteIndividualScreenshot) }

            val dataSetPosition = requireArguments().getInt(DATA_SET_POSITION_IN)

            setNegativeButton("No, discard") { _, _ -> triggerOnProcedureSelected(dataSetPosition) }
            setPositiveButton("Yes") { _, _ ->
                cropBundleProcessingJob = lifecycleScope.executeAsyncTask(
                    { saveCrop(
                        dataSetPosition,
                        BooleanUserPreferences.deleteIndividualScreenshot,
                        ViewModelProvider(requireActivity() as ExaminationActivity)[ExaminationActivityViewModel::class.java])
                    }
                )
                triggerOnProcedureSelected(dataSetPosition)
            }

            create()
        }

    private fun saveCrop(dataSetPosition: Int, deleteScreenshot: Boolean, sharedViewModel: ExaminationActivityViewModel): Void?{
        sharedViewModel.processCropBundle(dataSetPosition, deleteScreenshot, requireContext())
        return null
    }

    private fun triggerOnProcedureSelected(dataSetPosition: Int) =
        requireActivity().supportFragmentManager.setFragmentResult(
            PROCEDURE_SELECTED,
            bundleOf(DATA_SET_POSITION_OUT to dataSetPosition)
        )
}
package com.autocrop.activities.examination.fragments.viewpager

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.autocrop.activities.examination.ExaminationActivityViewModel
import com.autocrop.activities.examination.saveCropAndDeleteScreenshotIfApplicable
import com.autocrop.global.BooleanUserPreferences
import com.autocrop.utils.executeAsyncTask

/**
 * Class accounting for procedure dialog display upon screen click,
 * defining respective procedure effects
 */
class CropProcedureDialog
        : DialogFragment() {

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
                BooleanUserPreferences.deleteIndividualScreenshot)){ _, _, _ -> BooleanUserPreferences.toggle(
                BooleanUserPreferences.Keys.deleteIndividualScreenshot) }

            val dataSetPosition = requireArguments().getInt(DATA_SET_POSITION_IN)
            val triggerOnProcedureSelected = {
                requireActivity().supportFragmentManager.setFragmentResult(
                    PROCEDURE_SELECTED,
                    bundleOf(DATA_SET_POSITION_OUT to dataSetPosition)
                )
            }

            setNegativeButton("No, discard") { _, _ -> triggerOnProcedureSelected() }
            setPositiveButton("Yes") { _, _ ->
                lifecycleScope.executeAsyncTask({ saveCrop(dataSetPosition, BooleanUserPreferences.deleteIndividualScreenshot) })
                triggerOnProcedureSelected()
            }

            create()
        }

    private fun saveCrop(dataSetPosition: Int, deleteScreenshot: Boolean): Void?{
        val writeUri = ExaminationActivityViewModel.cropBundles[dataSetPosition].run {
            saveCropAndDeleteScreenshotIfApplicable(screenshotUri, crop, deleteScreenshot, requireContext().contentResolver)
        }

        sharedViewModel.setCropWriteDirPathIfApplicable(writeUri)
        sharedViewModel.incrementImageFileIOCounters(deleteScreenshot)
        return null
    }
}
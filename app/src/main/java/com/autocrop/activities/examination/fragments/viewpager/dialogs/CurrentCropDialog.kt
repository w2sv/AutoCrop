package com.autocrop.activities.examination.fragments.viewpager.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.autocrop.activities.examination.ExaminationActivityViewModel
import com.autocrop.preferences.BooleanPreferences
import com.autocrop.retriever.viewmodel.ViewModelRetriever
import com.autocrop.uielements.ExtendedDialogFragment
import com.autocrop.utils.executeAsyncTask

/**
 * Class accounting for procedure dialog display upon screen click,
 * defining respective procedure effects
 */
class CurrentCropDialog :
    ExtendedDialogFragment(),
    ViewModelRetriever<ExaminationActivityViewModel> {

    companion object{
        const val DATA_SET_POSITION_ARG_KEY = "DATA_SET_POSITION"
        const val PROCEDURE_SELECTED = "ON_PROCEDURE_SELECTED"
    }

    override val sharedViewModel: ExaminationActivityViewModel by activityViewModels()

    private val dataSetPosition: Int by lazy {
        requireArguments().getInt(DATA_SET_POSITION_ARG_KEY)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(activity).run {
            setTitle("Save crop?")

            setMultiChoiceItems(
                arrayOf("Delete corresponding screenshot"),
                booleanArrayOf(BooleanPreferences.deleteScreenshots)
            ){ _, _, _ ->
                BooleanPreferences.deleteScreenshots = !BooleanPreferences.deleteScreenshots
            }

            setNegativeButton("No, discard") { _, _ -> setFragmentResult() }
            setPositiveButton("Yes") { _, _ ->
                sharedViewModel.singleCropSavingJob = lifecycleScope.executeAsyncTask(
                    {
                        sharedViewModel.processCropBundle(
                            dataSetPosition,
                            BooleanPreferences.deleteScreenshots,
                            requireContext()
                        )
                    }
                )
                setFragmentResult()
            }

            create()
        }

    private fun setFragmentResult(){
        requireActivity().supportFragmentManager.setFragmentResult(
            PROCEDURE_SELECTED,
            bundleOf(DATA_SET_POSITION_ARG_KEY to dataSetPosition)
        )
    }
}
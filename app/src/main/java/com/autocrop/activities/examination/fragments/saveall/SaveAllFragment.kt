package com.autocrop.activities.examination.fragments.saveall

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.autocrop.activities.examination.ExaminationActivityViewModel
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.autocrop.global.BooleanUserPreferences
import com.autocrop.utils.executeAsyncTask
import com.w2sv.autocrop.databinding.ExaminationFragmentSaveallBinding

class SaveAllFragment :
    ExaminationActivityFragment<ExaminationFragmentSaveallBinding>(ExaminationFragmentSaveallBinding::class.java) {

    /**
     * Launch async [processRemainingCropBundles] task, call [castedActivity].invokeSubsequentFragment
     * onPostExecute
     */
    override fun onViewCreatedCore(savedInstanceState: Bundle?) {
        lifecycleScope.executeAsyncTask(
            { processRemainingCropBundles(BooleanUserPreferences.deleteScreenshots)},
            { castedActivity.invokeSubsequentFragment() }
        )
    }

    private fun processRemainingCropBundles(deleteCorrespondingScreenshots: Boolean): Void? {
        ExaminationActivityViewModel.cropBundles.indices.forEach {
            sharedViewModel.processCropBundle(it, deleteCorrespondingScreenshots, requireContext())
        }
        return null
    }
}
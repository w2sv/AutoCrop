package com.autocrop.activities.examination.fragments.saveall

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.autocrop.activities.examination.ExaminationActivityViewModel
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.autocrop.global.BooleanUserPreferences
import com.autocrop.utils.executeAsyncTask
import com.w2sv.autocrop.databinding.ActivityExaminationFragmentSaveallBinding

class SaveAllFragment :
    ExaminationActivityFragment<ActivityExaminationFragmentSaveallBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.executeAsyncTask(
            { processRemainingCropBundles(BooleanUserPreferences.deleteScreenshots)},
            { typedActivity.invokeSubsequentFragment() }
        )
    }

    private fun processRemainingCropBundles(deleteCorrespondingScreenshots: Boolean): Void? {
        ExaminationActivityViewModel.cropBundles.indices.forEach {
            sharedViewModel.processCropBundle(it, deleteCorrespondingScreenshots, requireContext())
        }
        return null
    }
}
package com.autocrop.activities.examination.fragments.saveall

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.autocrop.activities.examination.ExaminationActivityViewModel
import com.autocrop.activities.examination.fragments.ExaminationActivityFragment
import com.autocrop.preferences.BooleanPreferences
import com.autocrop.utils.kotlin.extensions.executeAsyncTask
import com.w2sv.autocrop.databinding.FragmentSaveallBinding

class SaveAllFragment :
    ExaminationActivityFragment<FragmentSaveallBinding>(FragmentSaveallBinding::class.java) {

    /**
     * Launch async [processRemainingCropBundles] task, call [typedActivity].invokeSubsequentFragment
     * onPostExecute
     */
    override fun onViewCreatedCore(savedInstanceState: Bundle?) {
        lifecycleScope.executeAsyncTask(
            { processRemainingCropBundles(BooleanPreferences.deleteScreenshots)},
            { typedActivity.invokeSubsequentFragment() }
        )
    }

    private fun processRemainingCropBundles(deleteCorrespondingScreenshots: Boolean): Void? {
        ExaminationActivityViewModel.cropBundles.indices.forEach {
            sharedViewModel.makeCropBundleProcessor(
                it,
                deleteCorrespondingScreenshots,
                requireContext()
            )
                .invoke()
        }
        return null
    }
}
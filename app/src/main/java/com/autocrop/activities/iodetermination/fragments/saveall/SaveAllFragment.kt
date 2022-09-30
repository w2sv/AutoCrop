package com.autocrop.activities.iodetermination.fragments.saveall

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.autocrop.activities.iodetermination.IODeterminationActivityViewModel
import com.autocrop.activities.iodetermination.fragments.IODeterminationActivityFragment
import com.autocrop.preferences.BooleanPreferences
import com.autocrop.utils.kotlin.extensions.executeAsyncTask
import com.w2sv.autocrop.databinding.FragmentSaveallBinding

class SaveAllFragment :
    IODeterminationActivityFragment<FragmentSaveallBinding>(FragmentSaveallBinding::class.java) {

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
        IODeterminationActivityViewModel.cropBundles.indices.forEach {
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
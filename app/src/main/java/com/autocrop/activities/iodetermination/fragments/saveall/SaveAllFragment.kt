package com.autocrop.activities.iodetermination.fragments.saveall

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.autocrop.activities.iodetermination.IODeterminationActivityViewModel
import com.autocrop.activities.iodetermination.fragments.IODeterminationActivityFragment
import com.autocrop.preferences.BooleanPreferences
import com.autocrop.utils.kotlin.extensions.executeAsyncTaskWithProgressUpdateReceiver
import com.w2sv.autocrop.databinding.FragmentSaveallBinding

class SaveAllFragment :
    IODeterminationActivityFragment<FragmentSaveallBinding>(FragmentSaveallBinding::class.java) {

    private val viewModel by viewModels<SaveAllViewModel>()

    /**
     * Launch async [processRemainingCropBundles] task, call [typedActivity].invokeSubsequentFragment
     * onPostExecute
     */
    override fun onViewCreatedCore(savedInstanceState: Bundle?) {
        viewModel.liveCropNumber.observe(viewLifecycleOwner){
            binding.progressTv.update(it)
        }

        lifecycleScope.executeAsyncTaskWithProgressUpdateReceiver(
            { processRemainingCropBundles(BooleanPreferences.deleteScreenshots, it)},
            { viewModel.liveCropNumber.increment() },
            { typedActivity.invokeSubsequentFragment() }
        )
    }

    private suspend fun processRemainingCropBundles(deleteCorrespondingScreenshots: Boolean, publishProgress: suspend (Void?) -> Unit): Void? {
        IODeterminationActivityViewModel.cropBundles.indices.forEach {
            sharedViewModel.makeCropBundleProcessor(
                it,
                deleteCorrespondingScreenshots,
                requireContext().contentResolver
            )
                .invoke()

            publishProgress(null)
        }
        return null
    }
}
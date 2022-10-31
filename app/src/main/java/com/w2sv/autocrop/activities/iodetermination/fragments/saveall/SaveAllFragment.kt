package com.w2sv.autocrop.activities.iodetermination.fragments.saveall

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.w2sv.autocrop.activities.iodetermination.IODeterminationActivityViewModel
import com.w2sv.autocrop.activities.iodetermination.fragments.IODeterminationActivityFragment
import com.w2sv.autocrop.databinding.FragmentSaveallBinding
import com.w2sv.autocrop.preferences.BooleanPreferences
import com.w2sv.autocrop.utils.android.extensions.postValue
import com.w2sv.kotlinutils.extensions.executeAsyncTaskWithProgressUpdateReceiver

class SaveAllFragment :
    IODeterminationActivityFragment<FragmentSaveallBinding>(FragmentSaveallBinding::class.java) {

    private val viewModel by viewModels<SaveAllViewModel>()

    /**
     * Launch async [processRemainingCropBundles] task, call [castActivity].invokeSubsequentFragment
     * onPostExecute
     */
    override fun onViewCreatedCore(savedInstanceState: Bundle?) {
        viewModel.liveCropNumber.observe(viewLifecycleOwner) {
            binding.progressTv.update(it)
        }

        lifecycleScope.executeAsyncTaskWithProgressUpdateReceiver(
            { processRemainingCropBundles(BooleanPreferences.deleteScreenshots, it) },
            {
                with(viewModel.liveCropNumber){
                    postValue(minOf(value!! + 1, viewModel.nImagesToBeSaved))
                }
            },
            { castActivity.invokeSubsequentFragment() }
        )
    }

    private suspend fun processRemainingCropBundles(
        deleteCorrespondingScreenshots: Boolean,
        publishProgress: suspend (Void?) -> Unit
    ): Void? {
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
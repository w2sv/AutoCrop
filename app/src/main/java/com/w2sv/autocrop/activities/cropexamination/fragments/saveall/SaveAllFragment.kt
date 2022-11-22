package com.w2sv.autocrop.activities.cropexamination.fragments.saveall

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.w2sv.autocrop.activities.cropexamination.CropExaminationActivity
import com.w2sv.autocrop.activities.cropexamination.CropExaminationActivityViewModel
import com.w2sv.autocrop.controller.ApplicationFragment
import com.w2sv.autocrop.databinding.FragmentSaveallBinding
import com.w2sv.autocrop.preferences.BooleanPreferences
import com.w2sv.autocrop.utils.android.extensions.postValue
import com.w2sv.kotlinutils.extensions.executeAsyncTaskWithProgressUpdateReceiver
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SaveAllFragment :
    ApplicationFragment<FragmentSaveallBinding>(FragmentSaveallBinding::class.java) {

    @Inject
    lateinit var booleanPreferences: BooleanPreferences

    class ViewModel : androidx.lifecycle.ViewModel() {
        val nImagesToBeSaved = CropExaminationActivityViewModel.cropBundles.size
        val liveCropNumber: LiveData<Int> by lazy {
            MutableLiveData(1)
        }
    }

    private val viewModel by viewModels<ViewModel>()
    private val activityViewModel by activityViewModels<CropExaminationActivityViewModel>()

    /**
     * Launch async [processRemainingCropBundles] task, call [castActivity].replaceWithSubsequentFragment
     * onPostExecute
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.liveCropNumber.observe(viewLifecycleOwner) {
            binding.progressTv.update(it)
        }

        lifecycleScope.executeAsyncTaskWithProgressUpdateReceiver(
            { processRemainingCropBundles(booleanPreferences.deleteScreenshots, it) },
            {
                with(viewModel.liveCropNumber) {
                    postValue(minOf(value!! + 1, viewModel.nImagesToBeSaved))
                }
            },
            { castActivity<CropExaminationActivity>().replaceWithSubsequentFragment() }
        )
    }

    private suspend fun processRemainingCropBundles(
        deleteCorrespondingScreenshots: Boolean,
        publishProgress: suspend (Void?) -> Unit
    ): Void? {
        CropExaminationActivityViewModel.cropBundles.indices.forEach {
            activityViewModel.makeCropBundleProcessor(
                it,
                deleteCorrespondingScreenshots,
                requireContext()
            )
                .invoke()

            publishProgress(null)
        }
        return null
    }
}
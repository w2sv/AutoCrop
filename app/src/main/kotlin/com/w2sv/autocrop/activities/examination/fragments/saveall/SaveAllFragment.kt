package com.w2sv.autocrop.activities.examination.fragments.saveall

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.w2sv.androidutils.extensions.postValue
import com.w2sv.autocrop.activities.ApplicationFragment
import com.w2sv.autocrop.activities.examination.ExaminationActivity
import com.w2sv.autocrop.databinding.FragmentSaveallBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class SaveAllFragment :
    ApplicationFragment<FragmentSaveallBinding>(FragmentSaveallBinding::class.java) {

    class ViewModel : androidx.lifecycle.ViewModel() {

        val nUnprocessedCrops = ExaminationActivity.ViewModel.cropBundles.size

        val liveCropNumber: LiveData<Int> by lazy {
            MutableLiveData(1)
        }
    }

    private val viewModel by viewModels<ViewModel>()
    private val activityViewModel by activityViewModels<ExaminationActivity.ViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.liveCropNumber.observe(viewLifecycleOwner) {
            binding.progressTv.update(it)
        }
    }

    override fun onResume() {
        super.onResume()

        launchCropProcessingCoroutine()
    }

    private fun launchCropProcessingCoroutine() {
        lifecycleScope.launch {
            ExaminationActivity.ViewModel.cropBundles.indices.forEach { bundleIndex ->
                withContext(Dispatchers.IO) {
                    activityViewModel.makeCropIOProcessor(
                        bundleIndex,
                        requireContext()
                    )
                        .invoke()
                }
                withContext(Dispatchers.Main) {
                    with(viewModel.liveCropNumber) {
                        postValue(minOf(value!! + 1, viewModel.nUnprocessedCrops))
                    }
                }
            }
            castActivity<ExaminationActivity>().replaceWithSubsequentFragment()
        }
    }
}
package com.w2sv.autocrop.activities.cropexamination.fragments.saveall

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.w2sv.androidutils.extensions.postValue
import com.w2sv.autocrop.activities.ApplicationFragment
import com.w2sv.autocrop.activities.cropexamination.CropExaminationActivity
import com.w2sv.autocrop.databinding.FragmentSaveallBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class SaveAllFragment :
    ApplicationFragment<FragmentSaveallBinding>(FragmentSaveallBinding::class.java) {

    class ViewModel : androidx.lifecycle.ViewModel() {

        val nUnprocessedCrops = CropExaminationActivity.ViewModel.cropBundles.size

        val liveCropNumber: LiveData<Int> by lazy {
            MutableLiveData(1)
        }
    }

    private val viewModel by viewModels<ViewModel>()
    private val activityViewModel by activityViewModels<CropExaminationActivity.ViewModel>()

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
            CropExaminationActivity.ViewModel.cropBundles.indices.forEach {
                withContext(Dispatchers.IO) {
                    activityViewModel.makeCropIOProcessor(
                        it,
                        requireContext().applicationContext
                    )
                        .invoke()
                }
                withContext(Dispatchers.Main) {
                    with(viewModel.liveCropNumber) {
                        postValue(minOf(value!! + 1, viewModel.nUnprocessedCrops))
                    }
                }
            }
            castActivity<CropExaminationActivity>().replaceWithSubsequentFragment()
        }
    }
}
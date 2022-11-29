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
import com.w2sv.autocrop.activities.cropexamination.CropExaminationActivity.ViewModel
import com.w2sv.autocrop.databinding.FragmentSaveallBinding
import com.w2sv.autocrop.preferences.BooleanPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class SaveAllFragment :
    ApplicationFragment<FragmentSaveallBinding>(FragmentSaveallBinding::class.java) {

    @Inject
    lateinit var booleanPreferences: BooleanPreferences

    class ViewModel : androidx.lifecycle.ViewModel() {
        val nUnsavedImages = CropExaminationActivity.ViewModel.cropBundles.size
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
                    activityViewModel.makeCropBundleProcessor(
                        it,
                        booleanPreferences.deleteScreenshots,
                        requireContext()
                    )
                        .invoke()
                }
                withContext(Dispatchers.Main) {
                    with(viewModel.liveCropNumber) {
                        postValue(minOf(value!! + 1, viewModel.nUnsavedImages))
                    }
                }
            }
            castActivity<CropExaminationActivity>().replaceWithSubsequentFragment()
        }
    }
}
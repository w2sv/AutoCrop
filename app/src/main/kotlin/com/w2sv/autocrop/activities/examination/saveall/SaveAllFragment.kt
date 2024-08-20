package com.w2sv.autocrop.activities.examination.saveall

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.w2sv.androidutils.lifecycle.increment
import com.w2sv.autocrop.activities.AppFragment
import com.w2sv.autocrop.activities.examination.ExaminationActivity
import com.w2sv.autocrop.databinding.SaveAllBinding
import com.w2sv.autocrop.utils.getFragment
import com.w2sv.autocrop.utils.requireCastActivity
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class SaveAllFragment :
    AppFragment<SaveAllBinding>(SaveAllBinding::class.java) {

    companion object {
        private const val EXTRA_CROP_BUNDLE_INDICES = "com.w2sv.autocrop.extra.CROP_BUNDLE_INDICES"

        fun getInstance(cropBundleIndices: ArrayList<Int>): SaveAllFragment =
            getFragment(SaveAllFragment::class.java, EXTRA_CROP_BUNDLE_INDICES to cropBundleIndices)
    }

    @HiltViewModel
    class ViewModel @Inject constructor(handle: SavedStateHandle) : androidx.lifecycle.ViewModel() {

        private val cropBundleIndices: ArrayList<Int> = handle[EXTRA_CROP_BUNDLE_INDICES]!!

        val nUnprocessedCrops: Int = cropBundleIndices.size

        val saveProgress: LiveData<Int> get() = _saveProgress
        private val _saveProgress = MutableLiveData(0)

        private val unprocessedCropBundleIndices: List<Int>
            get() =
                cropBundleIndices.run {
                    subList(saveProgress.value!!, size)
                }

        suspend fun saveAllCoroutine(processCropBundle: suspend (Int) -> Unit, onFinishedListener: () -> Unit) {
            coroutineScope {
                unprocessedCropBundleIndices.forEach { bundleIndex ->
                    withContext(Dispatchers.IO) {
                        processCropBundle(
                            bundleIndex
                        )
                    }
                    withContext(Dispatchers.Main) {
                        _saveProgress.increment()
                    }
                }

                onFinishedListener()
            }
        }
    }

    private val viewModel by viewModels<ViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.saveProgress.observe(viewLifecycleOwner) {
            binding.progressTv.updateText(
                minOf(it + 1, viewModel.nUnprocessedCrops),
                viewModel.nUnprocessedCrops
            )
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.saveAllCoroutine(
                    processCropBundle = {
                        activityViewModels<ExaminationActivity.ViewModel>().value.addCropBundleIOResult(
                            it,
                            requireContext()
                        )
                    },
                    onFinishedListener = { requireCastActivity<ExaminationActivity>().invokeExitFragmentOnNoCropProcessingJobRunning() }
                )
            }
        }
    }
}
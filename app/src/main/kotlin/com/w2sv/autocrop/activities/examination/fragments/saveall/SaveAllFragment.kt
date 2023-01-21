package com.w2sv.autocrop.activities.examination.fragments.saveall

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import com.w2sv.androidutils.extensions.increment
import com.w2sv.autocrop.activities.AppFragment
import com.w2sv.autocrop.activities.examination.ExaminationActivity
import com.w2sv.autocrop.databinding.FragmentSaveallBinding
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class SaveAllFragment :
    AppFragment<FragmentSaveallBinding>(FragmentSaveallBinding::class.java) {

    companion object {
        private const val EXTRA_CROP_BUNDLE_INDICES = "com.w2sv.autocrop.extra.CROP_BUNDLE_INDICES"

        fun getInstance(cropBundleIndices: ArrayList<Int>): SaveAllFragment =
            SaveAllFragment()
                .apply {
                    arguments = bundleOf(
                        EXTRA_CROP_BUNDLE_INDICES to cropBundleIndices
                    )
                }
    }

    @HiltViewModel
    class ViewModel @Inject constructor(handle: SavedStateHandle) : androidx.lifecycle.ViewModel() {

        private val cropBundleIndices: ArrayList<Int> = handle[EXTRA_CROP_BUNDLE_INDICES]!!

        val nUnprocessedCrops: Int = cropBundleIndices.size

        val progressLive: LiveData<Int> by lazy {
            MutableLiveData(0)
        }

        private val unprocessedCropBundleIndices: List<Int>
            get() =
                cropBundleIndices.run {
                    subList(progressLive.value!!, size)
                }

        suspend fun cropProcessingCoroutine(processCropBundle: (Int, Context) -> Unit, fragment: AppFragment<*>) {
            coroutineScope {
                unprocessedCropBundleIndices.forEach { bundleIndex ->
                    withContext(Dispatchers.IO) {
                        processCropBundle(
                            bundleIndex,
                            fragment.requireActivity()
                        )
                    }
                    withContext(Dispatchers.Main) {
                        progressLive.increment()
                    }
                }

                fragment.castActivity<ExaminationActivity>().invokeSubsequentController(fragment)
            }
        }
    }

    private val viewModel by viewModels<ViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.progressLive.observe(viewLifecycleOwner) {
            binding.progressTv.update(
                minOf(it + 1, viewModel.nUnprocessedCrops),
                viewModel.nUnprocessedCrops
            )
        }
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch {
            viewModel.cropProcessingCoroutine(
                activityViewModels<ExaminationActivity.ViewModel>().value::processCropBundle,
                this@SaveAllFragment
            )
        }
    }
}
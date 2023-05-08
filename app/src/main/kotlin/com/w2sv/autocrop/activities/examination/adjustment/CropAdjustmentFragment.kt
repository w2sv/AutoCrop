package com.w2sv.autocrop.activities.examination.adjustment

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.RectF
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.os.bundleOf
import androidx.core.text.color
import androidx.core.util.lruCache
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.w2sv.androidutils.lifecycle.postValue
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.AppFragment
import com.w2sv.autocrop.activities.examination.ExaminationActivity
import com.w2sv.autocrop.activities.examination.adjustment.extensions.asMappedFrom
import com.w2sv.autocrop.activities.examination.adjustment.extensions.asRectF
import com.w2sv.autocrop.activities.examination.adjustment.extensions.getRectF
import com.w2sv.autocrop.activities.examination.adjustment.extensions.maintainedPercentage
import com.w2sv.autocrop.activities.examination.adjustment.model.CropAdjustmentMode
import com.w2sv.autocrop.activities.examination.adjustment.model.EdgeSelectionState
import com.w2sv.autocrop.activities.examination.adjustment.model.Line
import com.w2sv.autocrop.activities.examination.adjustment.model.N_SCREEN_ORIENTATIONS
import com.w2sv.autocrop.databinding.CropAdjustmentBinding
import com.w2sv.autocrop.utils.getFragment
import com.w2sv.common.datastore.Repository
import com.w2sv.cropbundle.CropBundle
import com.w2sv.cropbundle.cropping.CropEdges
import com.w2sv.kotlinutils.extensions.getByOrdinal
import com.w2sv.kotlinutils.extensions.rounded
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.min

@AndroidEntryPoint
class CropAdjustmentFragment
    : AppFragment<CropAdjustmentBinding>(CropAdjustmentBinding::class.java) {

    companion object {
        const val REQUEST_KEY = "com.w2sv.autocrop.request_key.CROP_ADJUSTMENT_FRAGMENT_RESULT"

        fun getInstance(cropBundlePosition: Int): CropAdjustmentFragment =
            getFragment(
                CropAdjustmentFragment::class.java,
                CropBundle.EXTRA_POSITION to cropBundlePosition
            )
    }

    @HiltViewModel
    class ViewModel @Inject constructor(
        savedStateHandle: SavedStateHandle,
        contentResolver: ContentResolver,
        private val repository: Repository
    ) : androidx.lifecycle.ViewModel() {

        val cropBundle: CropBundle =
            ExaminationActivity.ViewModel.cropBundles[savedStateHandle[CropBundle.EXTRA_POSITION]!!]
        val screenshotBitmap: Bitmap = cropBundle.screenshot.getBitmap(contentResolver)

        /**
         * CropAdjustmentView dependencies
         */

        private val edgeCandidatePoints: FloatArray by lazy {
            cropBundle.edgeCandidates.map {
                listOf(
                    0f,
                    it.toFloat(),
                    screenshotBitmap.width.toFloat(),
                    it.toFloat()
                )
            }
                .flatten()
                .toFloatArray()
        }

        val initialCropRectF: RectF by lazy {
            cropBundle.crop.edges.asRectF(screenshotBitmap.width)
        }

        val imageRect: RectF by lazy {
            screenshotBitmap.getRectF()
        }

        val edgeCandidateLinesViewDomainCache = lruCache<Matrix, List<Line>>(
            N_SCREEN_ORIENTATIONS,
            create = { matrix ->
                FloatArray(edgeCandidatePoints.size)
                    .asMappedFrom(edgeCandidatePoints, matrix)
                    .toList()
                    .windowed(4, 4)
            }
        )

        val edgeCandidateYsViewDomainCache =
            lruCache<Matrix, List<Float>>(
                N_SCREEN_ORIENTATIONS,
                create = { matrix -> edgeCandidateLinesViewDomainCache.get(matrix).map { it[1] } }
            )

        /**
         * CropAdjustmentMode
         */

        val modeLive: MutableStateFlow<CropAdjustmentMode> by lazy {
            MutableStateFlow(getByOrdinal<CropAdjustmentMode>(repository.cropAdjustmentModeOrdinal.value))
                .apply {
                    viewModelScope.launch {
                        collect {
                            repository.cropAdjustmentModeOrdinal.value = it.ordinal
                        }
                    }
                }
        }

        /**
         * CropEdges
         */

        val cropEdgesLive: LiveData<CropEdges?> by lazy {
            MutableLiveData(cropBundle.crop.edges)
        }
        val cropEdgesChangedLive: LiveData<Boolean> by lazy {
            MutableLiveData(false)
        }

        fun resetCropEdges() {
            cropEdgesLive.postValue(cropBundle.crop.edges)
        }

        /**
         * Selected Edges
         */

        val edgeCandidatesSelectionState: LiveData<EdgeSelectionState> by lazy {
            MutableLiveData(EdgeSelectionState.Unselected)
        }
    }

    private val viewModel by viewModels<ViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.modeSwitch.isChecked = when (viewModel.modeLive.value) {
            CropAdjustmentMode.Manual -> false
            CropAdjustmentMode.EdgeSelection -> true
        }
        viewModel.setLiveDataObservers()
        binding.setOnClickListeners()
    }

    private fun ViewModel.setLiveDataObservers() {
        cropEdgesLive.observe(viewLifecycleOwner) { edges ->
            binding.onCropEdgesChanged(edges)
            cropEdgesChangedLive.postValue(edges != null && edges != cropBundle.crop.edges)
        }
        cropEdgesChangedLive.observe(viewLifecycleOwner) {
            binding.resetButton.isEnabled = it
            binding.applyButton.isEnabled = it
        }
        lifecycleScope.launch {
            modeLive.collect {
                binding.cropAdjustmentView.setModeConfig(it)
                binding.modeLabelTv.text = getString(it.labelRes)
                binding.resetButton.visibility = when (it) {
                    CropAdjustmentMode.EdgeSelection -> View.GONE
                    CropAdjustmentMode.Manual -> View.VISIBLE
                }
            }
        }
    }

    private fun CropAdjustmentBinding.onCropEdgesChanged(cropEdges: CropEdges?) {
        heightTv.text = formattedUnitText(
            "H",
            requireContext().getColor(R.color.highlight),
            cropEdges?.let {
                min(it.height, viewModel.screenshotBitmap.height)
            }
        )
        percentageTv.text =
            formattedUnitText(
                "%",
                requireContext().getColor(R.color.highlight),
                cropEdges?.let {
                    (viewModel.screenshotBitmap.maintainedPercentage(it.height.toFloat()) * 100).rounded(1)
                }
            )
    }

    private fun CropAdjustmentBinding.setOnClickListeners() {
        resetButton.setOnClickListener {
            cropAdjustmentView.reset()
        }
        modeSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.modeLive.value =
                if (isChecked)
                    CropAdjustmentMode.EdgeSelection
                else
                    CropAdjustmentMode.Manual
        }

        cancelButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        applyButton.setOnClickListener {
            setFragmentResult(REQUEST_KEY, bundleOf(CropEdges.EXTRA to viewModel.cropEdgesLive.value))
            requireActivity().supportFragmentManager.popBackStack()
        }
    }
}

private fun formattedUnitText(
    label: CharSequence,
    @ColorInt labelColor: Int,
    value: Any?
): SpannableStringBuilder =
    SpannableStringBuilder()
        .color(labelColor) {
            append(label)
        }
        .append(" ${value ?: "-"}")

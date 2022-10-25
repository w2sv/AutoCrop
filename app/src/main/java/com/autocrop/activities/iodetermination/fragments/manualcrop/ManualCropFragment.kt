package com.autocrop.activities.iodetermination.fragments.manualcrop

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.text.color
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.autocrop.CropEdges
import com.autocrop.activities.iodetermination.IODeterminationActivityViewModel
import com.autocrop.activities.iodetermination.fragments.IODeterminationActivityFragment
import com.autocrop.activities.iodetermination.fragments.manualcrop.utils.extensions.maintainedPercentage
import com.autocrop.utils.android.extensions.openBitmap
import com.autocrop.utils.kotlin.extensions.rounded
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.FragmentManualCropBinding
import kotlin.math.max
import kotlin.math.min

class ManualCropFragment
    : IODeterminationActivityFragment<FragmentManualCropBinding>(FragmentManualCropBinding::class.java) {

    companion object {
        private const val EXTRA_CROP_BUNDLE_INDEX = "com.w2sv.autocrop.CROP_BUNDLE_INDEX"
        private const val EXTRA_ADJUSTED_CROP_EDGES = "com.w2sv.autocrop.ADJUSTED_CROP_EDGES"
        const val KEY_RESULT = "com.w2sv.autocrop.CroppyFragment_RESULT"

        @JvmStatic
        fun instance(cropBundleIndex: Int): ManualCropFragment =
            ManualCropFragment().apply {
                arguments = bundleOf(
                    EXTRA_CROP_BUNDLE_INDEX to cropBundleIndex
                )
            }

        fun getAdjustedCropEdges(bundle: Bundle): CropEdges =
            @Suppress("DEPRECATION")
            bundle.getParcelable(EXTRA_ADJUSTED_CROP_EDGES)!!
    }

    private lateinit var viewModel: ManualCropViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cropBundleIndex = requireArguments().getInt(EXTRA_CROP_BUNDLE_INDEX)
        with(IODeterminationActivityViewModel.cropBundles[cropBundleIndex]) {
            viewModel = viewModels<ManualCropViewModel> {
                ManualCropViewModelFactory(
                    requireContext().contentResolver.openBitmap(screenshot.uri),
                    crop.edges,
                    screenshot.cropEdgesCandidates
                )
            }
                .value
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.setOnClickListeners()

        viewModel.cropEdges
            .observe(viewLifecycleOwner) { edges ->
                binding.onCropEdgesChanged(edges)
            }
    }

    private fun FragmentManualCropBinding.onCropEdgesChanged(cropEdges: CropEdges) {
        heightTv.text = styledUnit("H", min(cropEdges.height, viewModel.bitmap.height))
        y1Tv.text = styledUnit("Y1", max(cropEdges.top, 0))
        y2Tv.text = styledUnit("Y2", min(cropEdges.bottom, viewModel.bitmap.height))
        percentageTv.text =
            styledUnit("%", (viewModel.bitmap.maintainedPercentage(cropEdges.height.toFloat()) * 100).rounded(1))

        resetButton.visibility = if (cropEdges != viewModel.initialCropEdges)
            View.VISIBLE
        else
            View.GONE
    }

    private fun styledUnit(unit: String, value: Any): SpannableStringBuilder =
        SpannableStringBuilder()
            .color(requireContext().getColor(R.color.magenta_saturated)) { append(unit) }
            .append(" $value")

    private fun FragmentManualCropBinding.setOnClickListeners() {
        resetButton.setOnClickListener {
            cropView.reset()
        }

        cancelButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        applyButton.setOnClickListener {
            parentFragmentManager.popBackStackImmediate()
            setFragmentResult(
                KEY_RESULT,
                bundleOf(EXTRA_ADJUSTED_CROP_EDGES to viewModel.cropEdges.value!!)
            )
        }
    }
}
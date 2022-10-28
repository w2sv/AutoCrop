package com.autocrop.activities.iodetermination.fragments.manualcrop

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.text.color
import androidx.core.text.italic
import androidx.core.text.subscript
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.autocrop.CropBundle
import com.autocrop.CropEdges
import com.autocrop.activities.iodetermination.fragments.IODeterminationActivityFragment
import com.autocrop.activities.iodetermination.fragments.manualcrop.utils.extensions.maintainedPercentage
import com.autocrop.utils.android.extensions.loadBitmap
import com.autocrop.utils.kotlin.extensions.rounded
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.FragmentManualCropBinding
import kotlin.math.max
import kotlin.math.min

class ManualCropFragment
    : IODeterminationActivityFragment<FragmentManualCropBinding>(FragmentManualCropBinding::class.java) {

    companion object {
        private const val EXTRA_ADJUSTED_CROP_EDGES = "com.w2sv.autocrop.ADJUSTED_CROP_EDGES"
        const val KEY_RESULT = "com.w2sv.autocrop.CroppyFragment_RESULT"

        fun instance(cropBundle: CropBundle): ManualCropFragment =
            ManualCropFragment().apply {
                arguments = bundleOf(
                    CropBundle.EXTRA to cropBundle
                )
            }

        fun getAdjustedCropEdges(bundle: Bundle): CropEdges =
            @Suppress("DEPRECATION")
            bundle.getParcelable(EXTRA_ADJUSTED_CROP_EDGES)!!
    }

    private lateinit var viewModel: ManualCropViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @Suppress("DEPRECATION")
        with(requireArguments().getParcelable<CropBundle>(CropBundle.EXTRA)!!) {
            viewModel = viewModels<ManualCropViewModel> {
                ManualCropViewModel.Factory(
                    requireContext().contentResolver.loadBitmap(screenshot.uri),
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
        heightTv.text = styledUnitSpannableString("H", min(cropEdges.height, viewModel.bitmap.height))
        percentageTv.text =
            styledUnitSpannableString(
                "%",
                (viewModel.bitmap.maintainedPercentage(cropEdges.height.toFloat()) * 100).rounded(1)
            )

        y1Tv.text = styledUnitSpannableString(
            "Y",
            max(cropEdges.top, 0),
            1
        )
        y2Tv.text = styledUnitSpannableString(
            "Y",
            min(cropEdges.bottom, viewModel.bitmap.height),
            2
        )

        resetButton.visibility = if (cropEdges != viewModel.initialCropEdges)
            View.VISIBLE
        else
            View.GONE
    }

    private fun styledUnitSpannableString(
        unit: CharSequence,
        value: Any,
        unitSubscript: Any? = null
    ): SpannableStringBuilder =
        SpannableStringBuilder()
            .color(requireContext().getColor(R.color.magenta_bright)) {
                append(unit)
                unitSubscript?.let {
                    subscript {
                        append(it.toString())
                    }
                }
            }
            .italic {
                append(" $value")
            }

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
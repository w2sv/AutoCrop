package com.w2sv.autocrop.ui.screen.cropadjustment

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.os.bundleOf
import androidx.core.text.color
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.w2sv.autocrop.R
import com.w2sv.autocrop.AppFragment
import com.w2sv.autocrop.activities.examination.adjustment.extensions.maintainedPercentage
import com.w2sv.autocrop.databinding.CropAdjustmentBinding
import com.w2sv.autocrop.ui.screen.cropadjustment.extensions.maintainedPercentage
import com.w2sv.cropbundle.cropping.model.CropEdges
import com.w2sv.domain.model.CropAdjustmentMode
import com.w2sv.kotlinutils.rounded
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.min

@AndroidEntryPoint
class CropAdjustmentFragment
    : AppFragment<CropAdjustmentBinding>(CropAdjustmentBinding::class.java) {

    private val viewModel by viewModels<CropAdjustmentViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.modeSwitch.isChecked = when (viewModel.adjustmentMode.value) {
            CropAdjustmentMode.Manual -> false
            CropAdjustmentMode.EdgeSelection -> true
        }
        viewModel.setLiveDataObservers()
        binding.setOnClickListeners()
    }

    private fun CropAdjustmentViewModel.setLiveDataObservers() {
        cropEdges.observe(viewLifecycleOwner) { edges ->
            binding.onCropEdgesChanged(edges)
        }
        cropEdgesHaveChanged.observe(viewLifecycleOwner) {
            binding.resetButton.isEnabled = it
            binding.applyButton.isEnabled = it
        }
        lifecycleScope.launch {
            adjustmentMode.collect {
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
            viewModel.saveAdjustmentMode(
                if (isChecked)
                    CropAdjustmentMode.EdgeSelection
                else
                    CropAdjustmentMode.Manual
            )
        }

        cancelButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        applyButton.setOnClickListener {
            setFragmentResult(REQUEST_KEY, bundleOf(CropEdges.EXTRA to viewModel.cropEdges.value))
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
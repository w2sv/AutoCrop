package com.w2sv.autocrop.ui.screen.cropadjustment

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.text.color
import androidx.lifecycle.lifecycleScope
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.CropAdjustmentBinding
import com.w2sv.autocrop.ui.ViewBoundAppFragment
import com.w2sv.autocrop.ui.screen.cropSessionInjectedViewModel
import com.w2sv.autocrop.ui.screen.cropadjustment.extensions.maintainedPercentage
import com.w2sv.domain.model.CropAdjustmentMode
import com.w2sv.domain.model.CropEdges
import com.w2sv.kotlinutils.coroutines.flow.collectOn
import com.w2sv.kotlinutils.rounded
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.min

@AndroidEntryPoint
class CropAdjustmentFragment : ViewBoundAppFragment<CropAdjustmentBinding>(CropAdjustmentBinding::class.java) {

    private val viewModel by cropSessionInjectedViewModel<CropAdjustmentViewModel, CropAdjustmentViewModel.Factory>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            // Set up observers
            viewModel.adjustmentMode.collectOn(lifecycleScope) { onAdjustmentMode(it) }
            viewModel.cropEdges.observe(viewLifecycleOwner) { onCropEdgesChanged(it) }
            viewModel.cropEdgesHaveChanged.observe(viewLifecycleOwner) {
                resetButton.isEnabled = it
                applyButton.isEnabled = it
            }

            // Set up click listeners
            resetButton.setOnClickListener { cropAdjustmentView.reset() }
            modeSwitch.setOnCheckedChangeListener { _, isChecked ->
                viewModel.saveAdjustmentMode(
                    if (isChecked) {
                        CropAdjustmentMode.EdgeSelection
                    } else {
                        CropAdjustmentMode.Manual
                    }
                )
            }
            cancelButton.setOnClickListener { navController.popBackStack() }
            applyButton.setOnClickListener {
                viewModel.applyAdjustedEdges()
                navController.popBackStack()
            }
        }
    }

    private fun CropAdjustmentBinding.onAdjustmentMode(mode: CropAdjustmentMode) {
        modeSwitch.isChecked = when (mode) {
            CropAdjustmentMode.Manual -> false
            CropAdjustmentMode.EdgeSelection -> true
        }
        cropAdjustmentView.setModeConfig(mode)
        modeLabelTv.text = getString(mode.labelRes)
        resetButton.visibility = when (mode) {
            CropAdjustmentMode.EdgeSelection -> View.GONE
            CropAdjustmentMode.Manual -> View.VISIBLE
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

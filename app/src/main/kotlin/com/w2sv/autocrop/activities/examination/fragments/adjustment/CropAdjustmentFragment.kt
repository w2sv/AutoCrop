package com.w2sv.autocrop.activities.examination.fragments.adjustment

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.text.color
import androidx.core.text.italic
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.w2sv.androidutils.extensions.postValue
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.AppFragment
import com.w2sv.autocrop.activities.examination.ExaminationActivity
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.asRectF
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.maintainedPercentage
import com.w2sv.autocrop.databinding.CropAdjustmentBinding
import com.w2sv.autocrop.utils.getFragment
import com.w2sv.cropbundle.CropBundle
import com.w2sv.cropbundle.cropping.CropEdges
import com.w2sv.kotlinutils.extensions.rounded
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.min

@AndroidEntryPoint
class CropAdjustmentFragment
    : AppFragment<CropAdjustmentBinding>(CropAdjustmentBinding::class.java) {

    companion object {
        fun getInstance(cropBundlePosition: Int): CropAdjustmentFragment =
            getFragment(
                CropAdjustmentFragment::class.java,
                CropBundle.EXTRA_POSITION to cropBundlePosition
            )
    }

    @HiltViewModel
    class ViewModel @Inject constructor(
        savedStateHandle: SavedStateHandle,
        contentResolver: ContentResolver
    ) : androidx.lifecycle.ViewModel() {

        /**
         * Retrieved
         */

        val cropBundle: CropBundle =
            ExaminationActivity.ViewModel.cropBundles[savedStateHandle[CropBundle.EXTRA_POSITION]!!]
        val screenshotBitmap: Bitmap = cropBundle.screenshot.getBitmap(contentResolver)

        /**
         * Transformed CropAdjustmentView dependencies
         */

        val edgeCandidatePoints: FloatArray by lazy {
            cropBundle.screenshot.cropEdgeCandidates.map {
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

        /**
         * LiveData
         */

        val resetModeLive: LiveData<CropAdjustmentMode> by lazy {
            MutableLiveData()
        }

        var drawMode: CropAdjustmentMode? = CropAdjustmentMode.Manual

        val cropEdgesLive: LiveData<CropEdges> by lazy {
            MutableLiveData(cropBundle.crop.edges)
        }
        val cropEdgesChangedLive: LiveData<Boolean> by lazy {
            MutableLiveData(false)
        }

        fun resetCropEdges() {
            cropEdgesLive.postValue(cropBundle.crop.edges)
            cropEdgesChangedLive.postValue(false)
        }
    }

    private val viewModel by viewModels<ViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.setLiveDataObservers()
        binding.setOnClickListeners()
    }

    private fun ViewModel.setLiveDataObservers() {
        cropEdgesLive.observe(viewLifecycleOwner) { edges ->
            binding.onCropEdgesChanged(edges)
            cropEdgesChangedLive.postValue(edges != cropBundle.crop.edges)
        }
        cropEdgesChangedLive.observe(viewLifecycleOwner) {
            binding.resetButton.visibility =
                if (it)
                    View.VISIBLE
                else
                    View.GONE
            binding.applyButton.isEnabled = it
        }
        resetModeLive.observe(viewLifecycleOwner){
            binding.cropAdjustmentView.reset(it)
        }
    }

    private fun CropAdjustmentBinding.onCropEdgesChanged(cropEdges: CropEdges) {
        heightTv.text = formattedUnitText(
            "H",
            requireContext().getColor(R.color.highlight),
            min(cropEdges.height, viewModel.screenshotBitmap.height)
        )
        percentageTv.text =
            formattedUnitText(
                "%",
                requireContext().getColor(R.color.highlight),
                (viewModel.screenshotBitmap.maintainedPercentage(cropEdges.height.toFloat()) * 100).rounded(1)
            )
    }

    private fun CropAdjustmentBinding.setOnClickListeners() {
        resetButton.setOnClickListener {
            cropAdjustmentView.reset(viewModel.resetModeLive.value!!)
        }
        modeSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.resetModeLive.postValue(
                if (isChecked)
                    CropAdjustmentMode.EdgeSelection
                else
                    CropAdjustmentMode.Manual
            )
            viewModel.drawMode = null
        }

        cancelButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        applyButton.setOnClickListener {
            parentFragmentManager.popBackStackImmediate()  // TODO

            // notify ResultListener
            (requireViewBoundFragmentActivity().getCurrentFragment() as ResultListener)
                .onApplyAdjustedCropEdges(viewModel.cropEdgesLive.value!!)
        }
    }

    interface ResultListener {
        fun onApplyAdjustedCropEdges(cropEdges: CropEdges)
    }
}

private fun formattedUnitText(
    label: CharSequence,
    @ColorInt labelColor: Int,
    value: Any
): SpannableStringBuilder =
    SpannableStringBuilder()
        .color(labelColor) {
            append(label)
        }
        .italic {
            append(" $value")
        }
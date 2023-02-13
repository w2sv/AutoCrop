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
import androidx.core.text.subscript
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
import kotlin.math.max
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

        val cropEdgesLive: LiveData<CropEdges> by lazy {
            MutableLiveData(cropBundle.crop.edges)
        }
        val cropEdgesChangedLive: LiveData<Boolean> by lazy {
            MutableLiveData(false)
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
        }
    }

    private fun CropAdjustmentBinding.onCropEdgesChanged(cropEdges: CropEdges) {
        val unitColor = requireContext().getColor(R.color.highlight)

        heightTv.text = formattedUnitText(
            "H",
            unitColor,
            min(cropEdges.height, viewModel.screenshotBitmap.height)
        )
        percentageTv.text =
            formattedUnitText(
                "%",
                unitColor,
                (viewModel.screenshotBitmap.maintainedPercentage(cropEdges.height.toFloat()) * 100).rounded(1)
            )
        y1Tv.text = formattedUnitText(
            "Y",
            unitColor,
            max(cropEdges.top, 0),
            1
        )
        y2Tv.text = formattedUnitText(
            "Y",
            unitColor,
            min(cropEdges.bottom, viewModel.screenshotBitmap.height),
            2
        )
    }

    private fun CropAdjustmentBinding.setOnClickListeners() {
        resetButton.setOnClickListener {
            cropView.reset()
        }
        cancelButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        applyButton.setOnClickListener {
            // TODO
            parentFragmentManager.popBackStackImmediate()

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
    value: Any,
    unitSubscript: Any? = null
): SpannableStringBuilder =
    SpannableStringBuilder()
        .color(labelColor) {
            append(label)
            unitSubscript?.let {
                subscript {
                    append(it.toString())
                }
            }
        }
        .italic {
            append(" $value")
        }
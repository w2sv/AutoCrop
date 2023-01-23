package com.w2sv.autocrop.activities.examination.fragments.adjustment

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.View
import androidx.core.text.color
import androidx.core.text.italic
import androidx.core.text.subscript
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.AppFragment
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.maintainedPercentage
import com.w2sv.autocrop.activities.getFragment
import com.w2sv.autocrop.cropbundle.CropBundle
import com.w2sv.autocrop.cropbundle.cropping.CropEdges
import com.w2sv.autocrop.cropbundle.io.extensions.loadBitmap
import com.w2sv.autocrop.databinding.FragmentCropAdjustmentBinding
import com.w2sv.kotlinutils.extensions.rounded
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

@AndroidEntryPoint
class CropAdjustmentFragment
    : AppFragment<FragmentCropAdjustmentBinding>(FragmentCropAdjustmentBinding::class.java) {

    companion object {
        fun getInstance(cropBundle: CropBundle): CropAdjustmentFragment =
            getFragment(CropAdjustmentFragment::class.java, CropBundle.EXTRA to cropBundle)
    }

    @HiltViewModel
    class ViewModel @Inject constructor(
        savedStateHandle: SavedStateHandle,
        @ApplicationContext context: Context
    ) : androidx.lifecycle.ViewModel() {

        val bitmap: Bitmap
        val initialCropEdges: CropEdges
        //        val cropEdgePairCandidates: List<CropEdges>

        init {
            with(savedStateHandle.get<CropBundle>(CropBundle.EXTRA)!!) {
                bitmap = context.contentResolver.loadBitmap(screenshot.uri)!!
                initialCropEdges = crop.edges
            }
        }

        val cropEdges: LiveData<CropEdges> by lazy {
            MutableLiveData(initialCropEdges)
        }
    }

    private val viewModel by viewModels<ViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.setOnClickListeners()

        viewModel.cropEdges
            .observe(viewLifecycleOwner) { edges ->
                binding.onCropEdgesChanged(edges)
            }
    }

    private fun FragmentCropAdjustmentBinding.onCropEdgesChanged(cropEdges: CropEdges) {
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
            .color(requireContext().getColor(R.color.highlight)) {
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

    private fun FragmentCropAdjustmentBinding.setOnClickListeners() {
        resetButton.setOnClickListener {
            cropView.reset()
        }
        cancelButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        applyButton.setOnClickListener {
            parentFragmentManager.popBackStackImmediate()

            // notify ResultListener
            (requireViewBoundFragmentActivity().getCurrentFragment() as ResultListener)
                .onCropAdjustment(viewModel.cropEdges.value!!)
        }
    }

    interface ResultListener {
        fun onCropAdjustment(cropEdges: CropEdges)
    }
}
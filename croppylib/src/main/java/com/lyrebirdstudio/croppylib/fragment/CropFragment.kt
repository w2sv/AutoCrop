package com.lyrebirdstudio.croppylib.fragment

import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toRect
import androidx.core.os.bundleOf
import androidx.core.text.color
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.lyrebirdstudio.croppylib.CropRequest
import com.lyrebirdstudio.croppylib.databinding.FragmentImageCropBinding
import com.lyrebirdstudio.croppylib.utils.bitmap.resizedBitmap
import com.lyrebirdstudio.croppylib.utils.extensions.hideSystemBars
import com.lyrebirdstudio.croppylib.utils.extensions.maintainedPercentage
import com.lyrebirdstudio.croppylib.utils.extensions.rounded
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class CropFragment : Fragment() {

    private val binding: FragmentImageCropBinding get() = _binding!!
    private var _binding: FragmentImageCropBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageCropBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()

        requireActivity().hideSystemBars()
    }

    private val viewModel by viewModels<CropViewModel>{
        val cropRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(KEY_BUNDLE_CROP_REQUEST, CropRequest::class.java)!!
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(KEY_BUNDLE_CROP_REQUEST)!!
        }

        CropViewModelFactory(
            resizedBitmap(cropRequest.uri, requireContext()),
            cropRequest.initialCropRect,
            cropRequest.cropEdgePairCandidates,
            cropRequest.croppyTheme
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.setOnClickListeners()

        binding.resetButton.setTextColor(requireContext().getColor(viewModel.croppyTheme.accentColor))

        viewModel.cropRectF
            .observe(viewLifecycleOwner){ cropRectF ->
                binding.heightTv.text = styledText("H", min(cropRectF.height().roundToInt(), viewModel.bitmap.height))
                binding.y1Tv.text = styledText("Y1", max(cropRectF.top.roundToInt(), 0))
                binding.y2Tv.text = styledText("Y2", min(cropRectF.bottom.roundToInt(), viewModel.bitmap.height))
                binding.percentageTv.text = styledText("%", (viewModel.bitmap.maintainedPercentage(cropRectF.height()) * 100).rounded(1))

                binding.resetButton.visibility = if (cropRectF != viewModel.initialCropRect)
                    View.VISIBLE
                else
                    View.GONE
            }
    }

    private fun styledText(unit: String, value: Any): SpannableStringBuilder =
        SpannableStringBuilder()
            .color(requireContext().getColor(viewModel.croppyTheme.accentColor)) {append(unit)}
            .append(" $value")

    lateinit var onApplyClicked: ((Rect) -> Unit)
    lateinit var onCancelClicked: (() -> Unit)

    private fun FragmentImageCropBinding.setOnClickListeners(){
        cancelButton.setOnClickListener {
            onCancelClicked()
        }

        applyButton.setOnClickListener {
            onApplyClicked(viewModel.cropRectF.value!!.toRect())
        }

        resetButton.setOnClickListener {
            cropView.reset()
        }
    }

    companion object {
        private const val KEY_BUNDLE_CROP_REQUEST = "KEY_BUNDLE_CROP_REQUEST"

        @JvmStatic
        fun instance(cropRequest: CropRequest): CropFragment =
            CropFragment().apply {
                arguments = bundleOf(
                    KEY_BUNDLE_CROP_REQUEST to cropRequest
                )
            }
    }
}
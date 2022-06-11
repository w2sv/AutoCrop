package com.lyrebirdstudio.croppylib.fragment

import android.graphics.Rect
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toRect
import androidx.core.text.color
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.lyrebirdstudio.croppylib.CropRequest
import com.lyrebirdstudio.croppylib.databinding.FragmentImageCropBinding
import com.lyrebirdstudio.croppylib.utils.bitmap.resizedBitmap
import com.lyrebirdstudio.croppylib.utils.extensions.asMutable
import com.lyrebirdstudio.croppylib.utils.extensions.hideSystemBars
import com.lyrebirdstudio.croppylib.utils.extensions.remove
import com.lyrebirdstudio.croppylib.utils.extensions.rounded
import kotlin.math.max
import kotlin.math.min

class ImageCropFragment : Fragment() {

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

    private val viewModel by viewModels<ImageCropViewModel>{
        val cropRequest = arguments?.getParcelable<CropRequest>(KEY_BUNDLE_CROP_REQUEST)!!

        ImageCropViewModelFactory(
            resizedBitmap(cropRequest.sourceUri, requireContext()),
            cropRequest
        )
    }

    lateinit var onApplyClicked: ((Rect) -> Unit)
    lateinit var onCancelClicked: (() -> Unit)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.setOnClickListeners()

        binding.cropView.initialize(
            viewModel.bitmap,
            viewModel.cropRequest.initialCropRect,
            viewModel.cropRequest.croppyTheme
        ){ cropRectF ->
            viewModel.cropRect.asMutable.postValue(cropRectF.toRect())
        }

        binding.resetButton.setTextColor(requireContext().getColor(viewModel.cropRequest.croppyTheme.accentColor))

        viewModel.cropRect
            .observe(viewLifecycleOwner){
                binding.heightTv.text = styledText("H", min(it.height(), viewModel.bitmap.height))
                binding.y1Tv.text = styledText("Y1", max(it.top, 0))
                binding.y2Tv.text = styledText("Y2", min(it.bottom, viewModel.bitmap.height))
                binding.percentageTv.text = styledText(
                    "%",
                    (100 - (viewModel.bitmap.height.toFloat() - it.height().toFloat()) / viewModel.bitmap.height.toFloat() * 100).rounded(1)
                )

                binding.resetButton.visibility = if (it != viewModel.cropRequest.initialCropRect)
                    View.VISIBLE
                else
                    View.GONE
            }
    }

    private fun styledText(unit: String, value: Any): SpannableStringBuilder =
        SpannableStringBuilder()
            .color(requireContext().getColor(viewModel.cropRequest.croppyTheme.accentColor)) {append(unit)}
            .append(" $value")

    private fun FragmentImageCropBinding.setOnClickListeners(){
        imageViewCancel.setOnClickListener {
            onCancelClicked()
        }

        imageViewApply.setOnClickListener {
            onApplyClicked(cropView.getCropRect())
        }

        resetButton.setOnClickListener {
            cropView.reset()
            it.remove()
        }
    }

    companion object {
        private const val KEY_BUNDLE_CROP_REQUEST = "KEY_BUNDLE_CROP_REQUEST"

        @JvmStatic
        fun newInstance(cropRequest: CropRequest): ImageCropFragment {
            return ImageCropFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(KEY_BUNDLE_CROP_REQUEST, cropRequest)
                }
            }
        }
    }
}
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
            viewModel.cropRequest.croppyTheme.accentColor
        ){ cropRectF ->
            viewModel.cropRect.asMutable.postValue(cropRectF.toRect())
        }

        viewModel.cropRect
            .observe(viewLifecycleOwner){
                binding.heightTv.text = styledText("H", it.height())
                binding.y1Tv.text = styledText("Y1", it.top)
                binding.y2Tv.text = styledText("Y2", it.bottom)
            }
    }

    private fun styledText(unit: String, value: Int): SpannableStringBuilder =
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
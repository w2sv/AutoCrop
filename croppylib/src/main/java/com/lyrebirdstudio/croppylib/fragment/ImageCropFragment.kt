package com.lyrebirdstudio.croppylib.fragment

import android.graphics.Rect
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.lyrebirdstudio.croppylib.R
import com.lyrebirdstudio.croppylib.databinding.FragmentImageCropBinding
import com.lyrebirdstudio.croppylib.CropRequest
import kotlin.math.roundToInt

class ImageCropFragment : Fragment() {

    private val binding: FragmentImageCropBinding get() = _binding!!
    private var _binding: FragmentImageCropBinding? = null

    private val viewModel by viewModels<ImageCropViewModel>()

    var onApplyClicked: ((Rect) -> Unit)? = null
    var onCancelClicked: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.cropRequest = arguments?.getParcelable(KEY_BUNDLE_CROP_REQUEST)!!
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentImageCropBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cropView.setTheme(viewModel.cropRequest.croppyTheme)

        binding.imageViewCancel.setOnClickListener {
            onCancelClicked?.invoke()
        }

        binding.imageViewApply.setOnClickListener {
            onApplyClicked?.invoke(binding.cropView.getCropRect())
        }

        binding.cropView.apply{
            onInitialized = {
                viewModel.updateCropSize(binding.cropView.getCropSizeOriginal())
            }

            observeCropRectOnOriginalBitmapChanged = {
                viewModel.updateCropSize(binding.cropView.getCropSizeOriginal())
            }
        }

        viewModel
            .getCropViewStateLiveData()
            .observe(viewLifecycleOwner){
                binding.heightDisplayButton.text = if (it.height?.isNaN() == true)
                    ""
                else
                    SpannableString("H ${it.height?.roundToInt()}").apply {
                        setSpan(
                            ForegroundColorSpan(ContextCompat.getColor(requireContext(), it.croppyTheme.accentColor)),
                            0,
                            1,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        setSpan(
                            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.white)),
                            1,
                            length - 1,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
            }

        viewModel
            .getResizedBitmapLiveData()
            .observe(viewLifecycleOwner) {
                binding.cropView.setBitmap(
                    it.bitmap,
                    viewModel.cropRequest.initialCropRect
                )
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
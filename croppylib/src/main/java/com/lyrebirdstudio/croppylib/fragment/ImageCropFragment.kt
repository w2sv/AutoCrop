package com.lyrebirdstudio.croppylib.fragment

import android.graphics.Rect
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.toRect
import androidx.core.text.color
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.lyrebirdstudio.croppylib.CropRequest
import com.lyrebirdstudio.croppylib.databinding.FragmentImageCropBinding
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
        ImageCropViewModelFactory(
            requireActivity().application, 
            arguments?.getParcelable(KEY_BUNDLE_CROP_REQUEST)!!
        )
    }

    lateinit var onApplyClicked: ((Rect) -> Unit)
    lateinit var onCancelClicked: (() -> Unit)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.setOnClickListeners()

        binding.cropView.apply{
            setAccentColor(viewModel.cropRequest.croppyTheme.accentColor)

            onCropRectSizeChanged = { cropRectF ->
                val cropRect = cropRectF.toRect()

                viewModel.cropHeight.asMutable.postValue(cropRect.height())
            }

            initialize(viewModel.resizedBitmap, viewModel.cropRequest.initialCropRect)
        }

        viewModel.cropHeight
            .observe(viewLifecycleOwner){
                SpannableStringBuilder()
                    .color(ContextCompat.getColor(requireContext(), viewModel.cropRequest.croppyTheme.accentColor)) {append("H")}
                    .append(" $it")
            }
    }

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
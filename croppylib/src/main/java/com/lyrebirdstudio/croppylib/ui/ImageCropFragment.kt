package com.lyrebirdstudio.croppylib.ui

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.lyrebirdstudio.croppylib.R
import com.lyrebirdstudio.croppylib.databinding.FragmentImageCropBinding
import com.lyrebirdstudio.croppylib.main.CropRequest
import com.lyrebirdstudio.croppylib.state.CropFragmentViewState
import com.lyrebirdstudio.croppylib.util.delegate.inflate

class ImageCropFragment : Fragment() {

    private val binding: FragmentImageCropBinding by inflate(R.layout.fragment_image_crop)
    private lateinit var viewModel: ImageCropViewModel

    var onApplyClicked: ((Rect) -> Unit)? = null
    var onCancelClicked: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[ImageCropViewModel::class.java]
            .apply {
                setCropRequest(
                    arguments?.getParcelable(KEY_BUNDLE_CROP_REQUEST) ?: CropRequest.empty()
                )
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewModel.getCropRequest()?.let {
            binding.cropView.setTheme(it.croppyTheme)
        }

        binding.imageViewCancel.setOnClickListener {
            onCancelClicked?.invoke()
        }

        binding.imageViewApply.setOnClickListener {
            onApplyClicked?.invoke(binding.cropView.getCropRect())
        }

        with(binding.cropView) {

            onInitialized = {
                viewModel.updateCropSize(binding.cropView.getCropSizeOriginal())
            }

            observeCropRectOnOriginalBitmapChanged = {
                viewModel.updateCropSize(binding.cropView.getCropSizeOriginal())
            }
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel
            .getCropViewStateLiveData()
            .observe(viewLifecycleOwner, this@ImageCropFragment::renderViewState)

        viewModel
            .getResizedBitmapLiveData()
            .observe(viewLifecycleOwner) {
                binding.cropView.setBitmap(
                    it.bitmap,
                    viewModel.getCropRequest()?.initialCropRect
                )
            }
    }

    private fun renderViewState(cropFragmentViewState: CropFragmentViewState) {
        binding.viewState = cropFragmentViewState
        binding.executePendingBindings()
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
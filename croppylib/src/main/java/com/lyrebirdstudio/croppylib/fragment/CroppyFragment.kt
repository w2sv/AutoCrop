package com.lyrebirdstudio.croppylib.fragment

import android.os.Build
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.text.color
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.lyrebirdstudio.croppylib.CropEdges
import com.lyrebirdstudio.croppylib.CroppyRequest
import com.lyrebirdstudio.croppylib.databinding.FragmentImageCropBinding
import com.lyrebirdstudio.croppylib.utils.bitmap.resizedBitmap
import com.lyrebirdstudio.croppylib.utils.extensions.hideSystemBars
import com.lyrebirdstudio.croppylib.utils.extensions.maintainedPercentage
import com.lyrebirdstudio.croppylib.utils.extensions.rounded
import kotlin.math.max
import kotlin.math.min

class CroppyFragment : Fragment() {

    private val binding: FragmentImageCropBinding get() = _binding!!
    private var _binding: FragmentImageCropBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View {
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

    private val viewModel by viewModels<CroppyFragmentViewModel>{
        val cropRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(KEY_BUNDLE_CROP_REQUEST, CroppyRequest::class.java)!!
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(KEY_BUNDLE_CROP_REQUEST)!!
        }

        CroppyFragmentViewModelFactory(
            resizedBitmap(cropRequest.uri, requireContext()),
            cropRequest.initialCropEdges,
            cropRequest.cropEdgePairCandidates,
            cropRequest.croppyTheme
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.setOnClickListeners()

        binding.resetButton.setTextColor(requireContext().getColor(viewModel.croppyTheme.accentColor))

        viewModel.cropEdges
            .observe(viewLifecycleOwner){ edges ->
                binding.heightTv.text = styledText("H", min(edges.height, viewModel.bitmap.height))
                binding.y1Tv.text = styledText("Y1", max(edges.top, 0))
                binding.y2Tv.text = styledText("Y2", min(edges.bottom, viewModel.bitmap.height))
                binding.percentageTv.text = styledText("%", (viewModel.bitmap.maintainedPercentage(edges.height.toFloat()) * 100).rounded(1))

                binding.resetButton.visibility = if (edges != viewModel.initialCropEdges)
                    View.VISIBLE
                else
                    View.GONE
            }
    }

    private fun styledText(unit: String, value: Any): SpannableStringBuilder =
        SpannableStringBuilder()
            .color(requireContext().getColor(viewModel.croppyTheme.accentColor)) {append(unit)}
            .append(" $value")

    lateinit var onApplyClicked: ((CropEdges) -> Unit)
    lateinit var onCancelClicked: (() -> Unit)

    private fun FragmentImageCropBinding.setOnClickListeners(){
        cancelButton.setOnClickListener {
            onCancelClicked()
        }

        applyButton.setOnClickListener {
            onApplyClicked(viewModel.cropEdges.value!!)
        }

        resetButton.setOnClickListener {
            cropView.reset()
        }
    }

    companion object {
        private const val KEY_BUNDLE_CROP_REQUEST = "KEY_BUNDLE_CROP_REQUEST"

        @JvmStatic
        fun instance(cropRequest: CroppyRequest): CroppyFragment =
            CroppyFragment().apply {
                arguments = bundleOf(
                    KEY_BUNDLE_CROP_REQUEST to cropRequest
                )
            }
    }
}
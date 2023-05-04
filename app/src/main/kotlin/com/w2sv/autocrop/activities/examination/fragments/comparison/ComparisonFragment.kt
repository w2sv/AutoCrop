package com.w2sv.autocrop.activities.examination.fragments.comparison

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import androidx.transition.TransitionListenerAdapter
import com.w2sv.androidutils.extensions.crossVisualize
import com.w2sv.androidutils.extensions.getLong
import com.w2sv.androidutils.extensions.postValue
import com.w2sv.androidutils.extensions.repostValue
import com.w2sv.androidutils.extensions.show
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.AppFragment
import com.w2sv.autocrop.activities.examination.ExaminationActivity
import com.w2sv.autocrop.activities.examination.fragments.comparison.model.DisplayedImage
import com.w2sv.autocrop.databinding.ComparisonBinding
import com.w2sv.autocrop.utils.extensions.launchAfterShortDelay
import com.w2sv.autocrop.utils.getFragment
import com.w2sv.common.preferences.DataStoreRepository
import com.w2sv.cropbundle.CropBundle
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@AndroidEntryPoint
class ComparisonFragment
    : AppFragment<ComparisonBinding>(ComparisonBinding::class.java) {

    companion object {
        fun getInstance(cropBundlePosition: Int): ComparisonFragment =
            getFragment(ComparisonFragment::class.java, CropBundle.EXTRA_POSITION to cropBundlePosition)
    }

    @HiltViewModel
    class ViewModel @Inject constructor(
        savedStateHandle: SavedStateHandle,
        contentResolver: ContentResolver,
        val dataStoreRepository: DataStoreRepository
    ) : androidx.lifecycle.ViewModel() {

        val cropBundle: CropBundle =
            ExaminationActivity.ViewModel.cropBundles[savedStateHandle[CropBundle.EXTRA_POSITION]!!]
        val screenshotBitmap: Bitmap = cropBundle.screenshot.getBitmap(contentResolver)

        var enterTransitionCompleted = false

        val displayedImageLive: LiveData<DisplayedImage> = MutableLiveData(DisplayedImage.Crop)
        val screenshotViewImageMatrixLive: LiveData<Matrix> = MutableLiveData()
    }

    private val viewModel by viewModels<ViewModel>()

    override fun onAttach(context: Context) {
        super.onAttach(context)

        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
            .setDuration(resources.getLong(R.integer.delay_medium))
            .setInterpolator(DecelerateInterpolator(1.5f))
            .addListener(
                object : TransitionListenerAdapter() {
                    override fun onTransitionEnd(transition: Transition) {
                        super.onTransitionEnd(transition)

                        if (!viewModel.enterTransitionCompleted) {
                            viewModel.enterTransitionCompleted = true
                            onEnterTransitionCompleted()
                        }
                    }
                }
            )
    }

    private fun onEnterTransitionCompleted() {
        launchAfterShortDelay {
            if (!viewModel.dataStoreRepository.comparisonInstructionsShown.value) {
                ComparisonScreenInstructionDialogFragment().show(childFragmentManager)
            }
            else
            // trigger display of displayedImageTv
                viewModel.displayedImageLive.repostValue()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.populate()
        binding.setListeners()
        viewModel.setLiveDataObservers()
    }

    private fun ComparisonBinding.populate() {
        cropIv.transitionName = viewModel.cropBundle.identifier()
        cropIv.setImageBitmap(viewModel.cropBundle.crop.bitmap)
        screenshotIv.setImageBitmap(viewModel.screenshotBitmap)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun ComparisonBinding.setListeners() {
        root.setOnTouchListener { v, event ->
            when (event.action) {
                ACTION_DOWN -> {
                    viewModel.displayedImageLive.postValue(DisplayedImage.Screenshot)
                    v.performClick()
                    true
                }

                ACTION_UP -> {
                    viewModel.displayedImageLive.postValue(DisplayedImage.Crop)
                    true
                }

                else -> false
            }
        }
    }

    private fun ViewModel.setLiveDataObservers() {
        screenshotViewImageMatrixLive.observe(viewLifecycleOwner) {
            binding.cropIv.alignWithScreenshotImageView(it, viewModel.cropBundle.crop.edges)
        }
        displayedImageLive.observe(viewLifecycleOwner) {
            when (it!!) {
                DisplayedImage.Screenshot -> crossVisualize(binding.cropIv, binding.screenshotIv)
                DisplayedImage.Crop -> crossVisualize(binding.screenshotIv, binding.cropIv)
            }

            if (viewModel.enterTransitionCompleted) {
                binding.displayedImageTv.setTextAndShow(it)
            }
        }
    }

    fun popFromFragmentManager(fragmentManager: FragmentManager) {
        binding.cropIv.show()
        fragmentManager.popBackStack()
    }
}
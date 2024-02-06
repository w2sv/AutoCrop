package com.w2sv.autocrop.activities.examination.comparison

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
import androidx.lifecycle.viewModelScope
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import androidx.transition.TransitionListenerAdapter
import com.w2sv.androidutils.lifecycle.postValue
import com.w2sv.androidutils.lifecycle.repostValue
import com.w2sv.androidutils.ui.dialogs.show
import com.w2sv.androidutils.ui.resources.getLong
import com.w2sv.androidutils.ui.views.crossVisualize
import com.w2sv.androidutils.ui.views.show
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.AppFragment
import com.w2sv.autocrop.activities.examination.ExaminationActivity
import com.w2sv.autocrop.activities.examination.comparison.model.ImageType
import com.w2sv.autocrop.databinding.ComparisonBinding
import com.w2sv.autocrop.utils.extensions.launchAfterShortDelay
import com.w2sv.autocrop.utils.getFragment
import com.w2sv.cropbundle.CropBundle
import com.w2sv.domain.repository.PreferencesRepository
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import javax.inject.Inject

@AndroidEntryPoint
class ComparisonFragment
    : AppFragment<ComparisonBinding>(ComparisonBinding::class.java) {

    @HiltViewModel
    class ViewModel @Inject constructor(
        savedStateHandle: SavedStateHandle,
        contentResolver: ContentResolver,
        preferencesRepository: PreferencesRepository
    ) : androidx.lifecycle.ViewModel() {

        val comparisonInstructionsShown =
            preferencesRepository.comparisonInstructionsShown.stateIn(viewModelScope, SharingStarted.Eagerly)

        val cropBundle: CropBundle =
            ExaminationActivity.ViewModel.cropBundles[savedStateHandle[CropBundle.EXTRA_POSITION]!!]
        val screenshotBitmap: Bitmap = cropBundle.screenshot.getBitmap(contentResolver)

        var enterTransitionCompleted = false

        val imageTypeLive: LiveData<ImageType> = MutableLiveData(ImageType.Crop)
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
            if (!viewModel.comparisonInstructionsShown.value) {
                ComparisonScreenInstructionDialogFragment().show(childFragmentManager)
            }
            else {
                // trigger display of displayedImageTv
                viewModel.imageTypeLive.repostValue()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.populate()
        binding.setListeners()
        viewModel.setLiveDataObservers()
    }

    private fun ComparisonBinding.populate() {
        cropIv.transitionName = viewModel.cropBundle.identifier
        cropIv.setImageBitmap(viewModel.cropBundle.crop.bitmap)
        screenshotIv.setImageBitmap(viewModel.screenshotBitmap)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun ComparisonBinding.setListeners() {
        root.setOnTouchListener { v, event ->
            when (event.action) {
                ACTION_DOWN -> {
                    viewModel.imageTypeLive.postValue(ImageType.Screenshot)
                    v.performClick()
                    true
                }

                ACTION_UP -> {
                    viewModel.imageTypeLive.postValue(ImageType.Crop)
                    true
                }

                else -> false
            }
        }
    }

    private fun ViewModel.setLiveDataObservers() {
        screenshotViewImageMatrixLive.observe(viewLifecycleOwner) {
            binding.cropIv.alignWithScreenshotIV(it, cropBundle.crop.edges)
        }
        imageTypeLive.observe(viewLifecycleOwner) {
            when (it!!) {
                ImageType.Screenshot -> crossVisualize(binding.cropIv, binding.screenshotIv)
                ImageType.Crop -> crossVisualize(binding.screenshotIv, binding.cropIv)
            }

            if (enterTransitionCompleted) {
                binding.displayedImageTv.setTextAndShow(it)
            }
        }
    }

    fun popFromFragmentManager(fragmentManager: FragmentManager) {
        binding.cropIv.show()
        fragmentManager.popBackStack()
    }

    companion object {
        fun getInstance(cropBundlePosition: Int): ComparisonFragment =
            getFragment(ComparisonFragment::class.java, CropBundle.EXTRA_POSITION to cropBundlePosition)
    }
}
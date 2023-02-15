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
import com.w2sv.androidutils.extensions.show
import com.w2sv.autocrop.R
import com.w2sv.autocrop.activities.AppFragment
import com.w2sv.autocrop.activities.examination.ExaminationActivity
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.getScaleY
import com.w2sv.autocrop.databinding.ComparisonBinding
import com.w2sv.autocrop.utils.getFragment
import com.w2sv.cropbundle.CropBundle
import com.w2sv.preferences.GlobalFlags
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
        val globalFlags: GlobalFlags
    ) : androidx.lifecycle.ViewModel() {

        val cropBundle: CropBundle =
            ExaminationActivity.ViewModel.cropBundles[savedStateHandle[CropBundle.EXTRA_POSITION]!!]
        val screenshotBitmap: Bitmap = cropBundle.screenshot.getBitmap(contentResolver)

        var enterTransitionCompleted: Boolean = false

        val displayScreenshotLive: LiveData<Boolean> = MutableLiveData()
        val screenshotViewMatrixLive: LiveData<Matrix> = MutableLiveData()
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

                        if (!viewModel.enterTransitionCompleted)
                            onEnterTransitionCompleted()
                    }
                }
            )
    }

    private fun onEnterTransitionCompleted() {
        launchAfterShortDelay {
            viewModel.enterTransitionCompleted = true

            if (!viewModel.globalFlags.comparisonInstructionsShown) {
                launchAfterShortDelay {
                    ComparisonInstructionDialog().show(childFragmentManager)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.populate()
        binding.setOnClickListeners()
        viewModel.setLiveDataObservers()
    }

    private fun ComparisonBinding.populate() {
        with(cropIv) {
            transitionName = viewModel.cropBundle.identifier()
            setImageBitmap(viewModel.cropBundle.crop.bitmap)
        }
        screenshotIv.setImageBitmap(viewModel.screenshotBitmap)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun ComparisonBinding.setOnClickListeners() {
        ivLayout.setOnTouchListener { v, event ->
            when (event.action) {
                ACTION_DOWN -> {
                    viewModel.displayScreenshotLive.postValue(true)
                    v.performClick()
                    true
                }

                ACTION_UP -> {
                    viewModel.displayScreenshotLive.postValue(false)
                    true
                }

                else -> false
            }
        }
    }

    private fun ViewModel.setLiveDataObservers() {
        screenshotViewMatrixLive.observe(viewLifecycleOwner) {
            with(binding.cropIv) {
                imageMatrix = it
                translationY = viewModel.cropBundle.crop.edges.top.toFloat() * it.getScaleY()
                postInvalidate()
            }
        }
        displayScreenshotLive.observe(viewLifecycleOwner) {
            if (it)
                crossVisualize(binding.cropIv, binding.screenshotIv)
            else
                crossVisualize(binding.screenshotIv, binding.cropIv)

            if (viewModel.enterTransitionCompleted)
                binding.displayedImageTv.setTextAndShow(it)
        }
    }

    fun popFromFragmentManager(fragmentManager: FragmentManager) {
        binding.cropIv.show()
        fragmentManager.popBackStack()
    }
}
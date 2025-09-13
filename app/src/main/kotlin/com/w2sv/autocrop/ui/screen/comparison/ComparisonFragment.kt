package com.w2sv.autocrop.ui.screen.comparison

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.doOnNextLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import androidx.transition.TransitionListenerAdapter
import com.w2sv.androidutils.res.getLong
import com.w2sv.androidutils.view.crossVisualize
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.ComparisonBinding
import com.w2sv.autocrop.ui.ViewBoundAppFragment
import com.w2sv.autocrop.ui.screen.cropSessionInjectedViewModel
import com.w2sv.autocrop.ui.screen.cropadjustment.extensions.getScaleY
import com.w2sv.autocrop.ui.util.postponeEnterTransition
import com.w2sv.autocrop.ui.util.registerOnBackPressedHandler
import com.w2sv.kotlinutils.coroutines.launchDelayed
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ComparisonFragment : ViewBoundAppFragment<ComparisonBinding>(ComparisonBinding::class.java) {

    private val viewModel by cropSessionInjectedViewModel<ComparisonViewModel, ComparisonViewModel.Factory>()
    private val navArgs by navArgs<ComparisonFragmentArgs>()
    private var enterTransitionCompleted = false

    override fun onAttach(context: Context) {
        super.onAttach(context)

        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
            ?.setDuration(resources.getLong(R.integer.delay_medium))
            ?.setInterpolator(DecelerateInterpolator(1.5f))
            ?.addListener(
                object : TransitionListenerAdapter() {
                    override fun onTransitionEnd(transition: Transition) {
                        super.onTransitionEnd(transition)

                        if (!enterTransitionCompleted) {
                            enterTransitionCompleted = true
                            lifecycleScope.launchDelayed(200) {
                                viewModel.repostImageType()
                            }
                        }
                    }
                }
            )

        registerOnBackPressedHandler {
            viewModel.setImageType(ImageType.Crop)
            navController.popBackStack()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        postponeEnterTransition(view)

        binding.apply {
            initializeScreenshotViewAndCropViewScaleAndPositioning()
            initializeCropView()
            setOnTouchEventListeners()

            viewModel.imageType.observe(viewLifecycleOwner) {
                when (it) {
                    ImageType.Original -> crossVisualize(cropIv, screenshotIv)
                    ImageType.Crop -> crossVisualize(screenshotIv, cropIv)
                }

                if (enterTransitionCompleted) {
                    displayedImageTv.setTextAndShow(it)
                }
            }
        }
    }

    private fun ComparisonBinding.initializeCropView() {
        cropIv.apply {
            transitionName = navArgs.transitionName
            setImageBitmap(viewModel.crop.bitmap)
        }
    }

    private fun ComparisonBinding.initializeScreenshotViewAndCropViewScaleAndPositioning() {
        screenshotIv.apply {
            setImageBitmap(viewModel.screenshotBitmap)
            doOnNextLayout {
                val screenshotViewMatrix = (it as AppCompatImageView).imageMatrix
                cropIv.apply {
                    imageMatrix = screenshotViewMatrix
                    translationY = viewModel.crop.edges.top.toFloat() * screenshotViewMatrix.getScaleY()
                    postInvalidate()
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun ComparisonBinding.setOnTouchEventListeners() {
        root.setOnTouchListener { v, event ->
            when (event.action) {
                ACTION_DOWN -> {
                    viewModel.setImageType(ImageType.Original)
                    v.performClick()
                    true
                }

                ACTION_UP -> {
                    viewModel.setImageType(ImageType.Crop)
                    true
                }

                else -> false
            }
        }
    }
}

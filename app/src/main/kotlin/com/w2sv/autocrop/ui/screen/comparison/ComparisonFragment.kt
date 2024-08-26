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
import androidx.fragment.app.viewModels
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import androidx.transition.TransitionListenerAdapter
import com.w2sv.androidutils.res.getLong
import com.w2sv.androidutils.view.crossVisualize
import com.w2sv.androidutils.view.dialogs.show
import com.w2sv.androidutils.view.show
import com.w2sv.autocrop.AppFragment
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.ComparisonBinding
import com.w2sv.autocrop.ui.screen.comparison.model.ImageType
import com.w2sv.autocrop.ui.screen.cropadjustment.extensions.getScaleY
import com.w2sv.autocrop.util.extensions.launchAfterShortDelay
import com.w2sv.autocrop.util.registerOnBackPressedHandler
import com.w2sv.cropbundle.CropBundle
import dagger.hilt.android.AndroidEntryPoint

val CropBundle.sharedElementTransitionName: String
    get() = hashCode().toString()

@AndroidEntryPoint
class ComparisonFragment
    : AppFragment<ComparisonBinding>(ComparisonBinding::class.java) {

    private val viewModel by viewModels<ComparisonViewModel>()

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

                        if (!viewModel.enterTransitionCompleted) {
                            viewModel.enterTransitionCompleted = true
                            onEnterTransitionCompleted()
                        }
                    }
                }
            )

        registerOnBackPressedHandler {
            binding.cropIv.show()
            parentFragmentManager.popBackStack()
        }
    }

    private fun onEnterTransitionCompleted() {
        launchAfterShortDelay {
            if (!viewModel.instructionsShown.value) {
                ComparisonScreenInstructionDialogFragment().show(childFragmentManager)
            }
            else {
                // trigger display of displayedImageTv
                viewModel.repostImageType()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            with(cropIv) {
                transitionName = viewModel.cropBundle.sharedElementTransitionName
                setImageBitmap(viewModel.cropBundle.crop.bitmap)
            }
            with(screenshotIv) {
                setImageBitmap(viewModel.screenshotBitmap)
                doOnNextLayout {
                    val matrix = (it as AppCompatImageView).imageMatrix
                    with(cropIv) {
                        imageMatrix = matrix
                        translationY = viewModel.cropBundle.crop.edges.top.toFloat() * matrix.getScaleY()
                        postInvalidate()
                    }
                }
            }

            root.setOnTouchListener { v, event ->
                when (event.action) {
                    ACTION_DOWN -> {
                        viewModel.postImageType(ImageType.Screenshot)
                        v.performClick()
                        true
                    }

                    ACTION_UP -> {
                        viewModel.postImageType(ImageType.Crop)
                        true
                    }

                    else -> false
                }
            }

            with(viewModel) {
                imageType.observe(viewLifecycleOwner) {
                    when (it!!) {
                        ImageType.Screenshot -> crossVisualize(cropIv, screenshotIv)
                        ImageType.Crop -> crossVisualize(screenshotIv, cropIv)
                    }

                    if (enterTransitionCompleted) {
                        displayedImageTv.setTextAndShow(it)
                    }
                }
            }
        }
    }
}
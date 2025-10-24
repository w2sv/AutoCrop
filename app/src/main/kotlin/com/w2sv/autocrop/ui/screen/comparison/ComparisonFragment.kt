package com.w2sv.autocrop.ui.screen.comparison

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.MotionEvent.ACTION_CANCEL
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnNextLayout
import androidx.lifecycle.lifecycleScope
import com.w2sv.androidutils.view.crossVisualize
import com.w2sv.autocrop.R
import com.w2sv.autocrop.databinding.ComparisonBinding
import com.w2sv.autocrop.ui.ViewBoundAppFragment
import com.w2sv.autocrop.ui.screen.cropSessionInjectedViewModel
import com.w2sv.autocrop.ui.util.hideSystemBars
import com.w2sv.autocrop.ui.util.postponeEnterTransition
import com.w2sv.autocrop.ui.util.registerOnBackPressedHandler
import com.w2sv.autocrop.ui.util.showSystemBars
import com.w2sv.autocrop.ui.util.view.getScaleY
import com.w2sv.autocrop.ui.util.view.inflateSharedElementTransition
import com.w2sv.autocrop.ui.util.view.onEnd
import com.w2sv.autocrop.ui.util.view.setDebouncedOnClickListener
import com.w2sv.autocrop.ui.views.FadeOutTextView
import com.w2sv.kotlinutils.coroutines.flow.collectLatestOn
import com.w2sv.kotlinutils.coroutines.launchDelayed
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ComparisonFragment : ViewBoundAppFragment<ComparisonBinding>(ComparisonBinding::class.java) {

    private val viewModel by cropSessionInjectedViewModel<ComparisonViewModel, ComparisonViewModel.Factory>()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        hideSystemBars()
        sharedElementEnterTransition = cropEnterTransition(context)
        registerOnBackPressedHandler(::onBack)
    }

    private fun cropEnterTransition(context: Context) =
        inflateSharedElementTransition(context)
            ?.onEnd {
                // Show instructions after short delay
                lifecycleScope.launchDelayed(200) {
                    viewModel.emitFadeOutTextArgs(
                        FadeOutTextView.Args(
                            textRes = com.w2sv.core.common.R.string.comparison_instruction,
                            iconRes = R.drawable.ic_info_24,
                            displayDuration = 3_000
                        )
                    )
                }
            }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        postponeEnterTransition(view)

        binding.apply {
            initializeScreenshotViewAndCropViewScaleAndPositioning()
            initializeCropView()
            setOnTouchEventListener()
            backButton.setDebouncedOnClickListener { onBack() }

            viewModel.fadeOutTextArgs.collectLatestOn(lifecycleScope) {
                displayedImageTv.setAndShow(it)
            }

            viewModel.imageType.observe(viewLifecycleOwner) {
                when (it) {
                    ComparisonImageType.Original -> crossVisualize(cropIv, screenshotIv)
                    ComparisonImageType.Crop -> crossVisualize(screenshotIv, cropIv)
                }
            }
        }
    }

    private fun ComparisonBinding.initializeCropView() {
        cropIv.apply {
            transitionName = viewModel.sharedElementTransitionName
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
    private fun ComparisonBinding.setOnTouchEventListener() {
        root.setOnTouchListenerIgnoringSystemBarAreas { view, event ->
            when (event.actionMasked) {
                ACTION_DOWN -> {
                    viewModel.setImageType(ComparisonImageType.Original)
                    // Lets accessibility services know the view was clicked, which enables those services to react to it
                    view.performClick()
                    true
                }

                ACTION_UP, ACTION_CANCEL -> {
                    viewModel.setImageType(ComparisonImageType.Crop)
                    true
                }

                else -> false
            }
        }
    }

    private fun onBack() {
        viewModel.setImageType(ComparisonImageType.Crop, displayFadeOutText = false)
        navController.popBackStack()
    }

    override fun onDestroy() {
        super.onDestroy()
        showSystemBars()
    }
}

@SuppressLint("ClickableViewAccessibility")
private fun View.setOnTouchListenerIgnoringSystemBarAreas(listener: View.OnTouchListener) {
    setOnTouchListener { view, event ->
        val insets = ViewCompat.getRootWindowInsets(view)
        val statusBarHeight = insets?.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.statusBars())?.top
            ?: 0
        val navBarHeight = insets?.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.navigationBars())?.bottom
            ?: 0

        // Ignore touches in system bar areas
        val y = event.y.toInt()
        if (y < statusBarHeight || y > view.height - navBarHeight) {
            return@setOnTouchListener false
        }

        listener.onTouch(view, event)
    }
}

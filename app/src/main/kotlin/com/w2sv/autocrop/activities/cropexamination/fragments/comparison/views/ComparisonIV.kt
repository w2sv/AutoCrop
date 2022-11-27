package com.w2sv.autocrop.activities.cropexamination.fragments.comparison.views

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout.LayoutParams
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.ViewCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.w2sv.androidutils.extensions.toggle
import com.w2sv.androidutils.extensions.viewModel
import com.w2sv.autocrop.activities.cropexamination.fragments.comparison.ComparisonFragment
import slimber.log.i

class ComparisonIV(context: Context, attributeSet: AttributeSet) :
    AppCompatImageView(context, attributeSet) {

    private val viewModel by viewModel<ComparisonFragment.ViewModel>()

    private lateinit var originalLayoutParams: LayoutParams

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        i{"onAttachedToWindow"}
        originalLayoutParams = layoutParams as LayoutParams

        if (!isInEditMode) {
            ViewCompat.setTransitionName(this, viewModel.cropBundle.identifier())

            viewModel.setLiveDataObservers(findViewTreeLifecycleOwner()!!)

            setOnClickListener {
                viewModel.displayScreenshot.toggle()
            }
        }
    }

    private fun ComparisonFragment.ViewModel.setLiveDataObservers(lifecycleOwner: LifecycleOwner) {
        useInsetLayoutParams.observe(lifecycleOwner) {
            setLayout(useInsetParams = it)
        }
        displayScreenshot.observe(lifecycleOwner) {
            setImage(displayScreenshot = it)
        }
    }

    private fun setImage(displayScreenshot: Boolean) {
        i { "setImage: displayScreenshot = $displayScreenshot" }
        if (displayScreenshot)
            setImageBitmap(viewModel.screenshotBitmap)
        else
            setCrop()
    }

    private fun setCrop() {
        if (viewModel.useInsetLayoutParams.value!!)
            setImageBitmap(viewModel.cropBundle.crop.bitmap)
                .also { i{"nonInsetCrop"} }
        else
            setImageDrawable(viewModel.cropInsetDrawable)
                .also { i{"insetCrop"} }
    }

    private fun setLayout(useInsetParams: Boolean) {
        i { "setLayout: useInsetParams = $useInsetParams" }
        if (useInsetParams)
            setInsetLayoutParams()
        else
            resetLayoutParams()

        requestLayout()
    }

    private fun setInsetLayoutParams() {
        layoutParams = LayoutParams(originalLayoutParams).apply {
            topMargin = viewModel.cropFittedInsets.top
            bottomMargin = viewModel.cropFittedInsets.bottom
        }
    }

    private fun resetLayoutParams() {
        layoutParams = originalLayoutParams
    }
}
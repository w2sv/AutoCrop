package com.autocrop.activities.iodetermination.fragments.comparison

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.InsetDrawable
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.ViewCompat
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.autocrop.utils.android.extensions.openBitmap
import com.autocrop.utils.android.extensions.viewModelLazy
import com.autocrop.utils.android.livedata.toggle

class ComparisonImageView(context: Context, attributeSet: AttributeSet):
    AppCompatImageView(context, attributeSet){

    private val viewModel by viewModelLazy<ComparisonViewModel>()

    private val screenshot: Bitmap by lazy {
        context.contentResolver.openBitmap(viewModel.cropBundle.screenshot.uri)
    }

    private val cropMargins: Array<Int> by lazy {
        viewModel.cropBundle.run {
            arrayOf(
                0,
                crop.edges.top,
                0,
                screenshot.height - crop.edges.bottom
            )
        }
    }

    private val insetCropDrawable by lazy {
        InsetDrawable(
            BitmapDrawable(resources, viewModel.cropBundle.crop.bitmap),
            cropMargins[0], cropMargins[1], cropMargins[2], cropMargins[3]
        )
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (!isInEditMode){
            ViewCompat.setTransitionName(this, viewModel.cropBundle.identifier())

            setOnClickListener{
                viewModel.displayScreenshot.toggle()
            }

            viewModel.displayScreenshot.observe(findViewTreeLifecycleOwner()!!){
                setDrawable(it, viewModel.enterTransitionCompleted)
            }
        }
    }

    private fun setDrawable(displayScreenshot: Boolean, enterTransitionCompleted: Boolean){
        if (displayScreenshot)
            setImageBitmap(screenshot)
        else
            setCrop(marginalized = !enterTransitionCompleted)
    }

    private fun setCrop(marginalized: Boolean = false){
        if (marginalized){
            layoutParams = marginalizedCropLayoutParams
            setImageBitmap(viewModel.cropBundle.crop.bitmap)
        }
        else
            setImageDrawable(insetCropDrawable)
    }

    private val marginalizedCropLayoutParams: RelativeLayout.LayoutParams by lazy {
        (layoutParams as RelativeLayout.LayoutParams).apply {
            setMargins(cropMargins[0], cropMargins[1], cropMargins[2], cropMargins[3])
        }
    }

    fun prepareSharedElementExitTransition(){
        setCrop(marginalized = true)
    }
}
package com.autocrop.activities.iodetermination.fragments.comparison

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.InsetDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.ViewCompat
import androidx.fragment.app.findFragment
import com.autocrop.utils.android.extensions.toggle

class ComparisonImageView(context: Context, attributeSet: AttributeSet):
    AppCompatImageView(context, attributeSet){

    private val viewModel by lazy {
        findFragment<ComparisonFragment>().viewModel
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (!isInEditMode){
            ViewCompat.setTransitionName(this, viewModel.cropBundle.identifier())

            setOnClickListener{
                viewModel.displayScreenshot.toggle()
            }
        }
    }

    fun setImage(displayScreenshot: Boolean){
        if (displayScreenshot)
            setImageBitmap(viewModel.screenshotBitmap)
        else
            setCrop(marginalized = !viewModel.conductedOnEnterTransitionCompleted)
    }

    fun setCrop(marginalized: Boolean = false){
        if (marginalized){
            layoutParams = marginalizedCropLayoutParams
            setImageBitmap(viewModel.cropBundle.crop.bitmap)
        }
        else
            setImageDrawable(insetCropBitmapDrawable)
    }

    private val marginalizedCropLayoutParams: RelativeLayout.LayoutParams by lazy {
        (layoutParams as RelativeLayout.LayoutParams).apply {
            setMargins(cropMargins[0], cropMargins[1], cropMargins[2], cropMargins[3])
        }
    }

    private val insetCropBitmapDrawable by lazy {
        InsetDrawable(
            BitmapDrawable(resources, viewModel.cropBundle.crop.bitmap),
            cropMargins[0], cropMargins[1], cropMargins[2], cropMargins[3]
        )
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

    fun resetLayoutParams(){
        layoutParams = (parent as View).layoutParams as RelativeLayout.LayoutParams
    }
}
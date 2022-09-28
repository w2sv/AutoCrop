package com.autocrop.activities.examination.fragments.comparison

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.InsetDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import com.autocrop.utils.android.livedata.toggle

inline fun <reified T: ViewModel>View.viewModel(type: Class<T>): T =
    ViewModelProvider(findViewTreeViewModelStoreOwner()!!)[type]

class ComparisonImageView(context: Context, attributeSet: AttributeSet):
    AppCompatImageView(context, attributeSet){

    private val viewModel by lazy{
        viewModel(ComparisonViewModel::class.java)
    }

    private val screenshot: Bitmap by lazy {
        viewModel.cropBundle.screenshot.bitmap(context.contentResolver)
    }

    private val cropMargins: Array<Int> by lazy {
        arrayOf(0, viewModel.cropBundle.crop.rect.top, 0, viewModel.cropBundle.crop.bottomOffset)
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
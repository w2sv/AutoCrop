package com.autocrop.activities.iodetermination.fragments.comparison

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.ViewCompat
import androidx.fragment.app.findFragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
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

            insetLayoutParams = viewModel.cropFittedInsets.run {
                (layoutParams as RelativeLayout.LayoutParams).apply {
                    setMargins(get(0), get(1), get(2), get(3))
                }
            }

            viewModel.setLiveDataObservers(findViewTreeLifecycleOwner()!!)

            setOnClickListener{
                viewModel.displayScreenshot.toggle()
            }
        }
    }

    private fun ComparisonViewModel.setLiveDataObservers(lifecycleOwner: LifecycleOwner){
        useInsetLayoutParams.observe(lifecycleOwner){
            if (it)
                setInsetLayoutParams()
            else
                resetLayoutParams()
        }
        displayScreenshot.observe(lifecycleOwner) {
            setImage(displayScreenshot = it)
        }
    }

    private fun setImage(displayScreenshot: Boolean){
        if (displayScreenshot)
            setImageBitmap(viewModel.screenshotBitmap)
        else
            setCrop()
    }

    private fun setCrop(){
        if (viewModel.useInsetLayoutParams.value!!)
            setImageBitmap(viewModel.cropBundle.crop.bitmap)
        else
            setImageDrawable(viewModel.cropInsetDrawable)
    }

    private fun setInsetLayoutParams(){
        layoutParams = insetLayoutParams
    }

    private lateinit var insetLayoutParams: RelativeLayout.LayoutParams

    private fun resetLayoutParams(){
        layoutParams = (parent as View).layoutParams as RelativeLayout.LayoutParams
    }
}
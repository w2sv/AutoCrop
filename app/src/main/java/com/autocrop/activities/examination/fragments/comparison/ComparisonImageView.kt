package com.autocrop.activities.examination.fragments.comparison

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.InsetDrawable
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.ViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import com.autocrop.activities.examination.fragments.viewpager.transitionName
import kotlin.properties.Delegates

class ComparisonImageView(context: Context, attributeSet: AttributeSet):
    AppCompatImageView(context, attributeSet){

    val sharedViewModel by lazy{  // TODO
        ViewModelProvider(findViewTreeViewModelStoreOwner()!!)[ComparisonViewModel::class.java]
    }

    private val screenshot: Bitmap by lazy {
        BitmapFactory.decodeStream(
            context.contentResolver.openInputStream(
                sharedViewModel.cropBundle.screenshot.uri
            )
        )
    }
    private val insetCropDrawable by lazy {
        InsetDrawable(
            BitmapDrawable(resources, sharedViewModel.cropBundle.crop.bitmap),
            0,
            sharedViewModel.cropBundle.crop.rect.top,
            0,
            sharedViewModel.cropBundle.bottomOffset
        )
    }

    private var displayingScreenshot by Delegates.notNull<Boolean>()

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        ViewCompat.setTransitionName(this, sharedViewModel.cropBundle.transitionName())

        setCrop(marginalized = true)
    }

    fun onSharedElementEnterTransitionEnd(rootLayoutParams: RelativeLayout.LayoutParams){
        layoutParams = rootLayoutParams
        setScreenshot()

        displayingScreenshot = true

        setOnClickListener{
            if (displayingScreenshot)
                setCrop()
            else
                setScreenshot()

            displayingScreenshot = !displayingScreenshot
        }
    }

    private fun setCrop(marginalized: Boolean = false){
        if (marginalized){
            layoutParams = marginalizedCropLayoutParams
            setImageBitmap(sharedViewModel.cropBundle.crop.bitmap)
        }
        else
            setImageDrawable(insetCropDrawable)
    }

    private val marginalizedCropLayoutParams: RelativeLayout.LayoutParams by lazy {
        (layoutParams as RelativeLayout.LayoutParams).apply {
            setMargins(
                0,
                sharedViewModel.cropBundle.crop.rect.top,
                0,
                sharedViewModel.cropBundle.bottomOffset
            )
        }
    }

    private fun setScreenshot(){
        setImageBitmap(screenshot)
    }

    fun prepareSharedElementExitTransition() =
        setCrop(marginalized = true)
}
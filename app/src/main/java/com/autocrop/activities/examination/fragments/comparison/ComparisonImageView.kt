package com.autocrop.activities.examination.fragments.comparison

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.InsetDrawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.findViewTreeViewModelStoreOwner

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

    private var displayingScreenshot: Boolean? = false

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        setOnClickListener {
            displayingScreenshot?.let {
                if (it)
                    showInsetCrop()
                else
                    showScreenshot()

                displayingScreenshot = !it
            }
        }
    }

    fun onSharedElementEnterTransitionEnd(rootLayoutParams: RelativeLayout.LayoutParams){
        showMarginalizedCrop()

//        Handler(Looper.getMainLooper()).postDelayed(
//            {
//                layoutParams = rootLayoutParams
//                showScreenshot()
//                    .also { displayingScreenshot = true }
//            },
//            250
//        )
    }

    fun prepareExitTransition() =
        showMarginalizedCrop()

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

    private fun showMarginalizedCrop(){
        layoutParams = marginalizedCropLayoutParams
        setImageBitmap(sharedViewModel.cropBundle.crop.bitmap)
    }

    private fun showScreenshot(){
        setImageBitmap(screenshot)
    }

    private fun showInsetCrop(){
        setImageDrawable(insetCropDrawable)
    }
}
package com.autocrop.activities.examination.fragments.comparison

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerViewModel
import com.autocrop.activities.examination.fragments.viewpager.views.ViewPagerViewModelRetriever
import com.autocrop.uicontroller.ViewModelRetriever

class OriginalScreenshotImageView(context: Context, attributeSet: AttributeSet) :
    AppCompatImageView(context, attributeSet),
    ViewModelRetriever<ViewPagerViewModel> by ViewPagerViewModelRetriever(context) {

    private val cropBundle = sharedViewModel.dataSet.currentCropBundle
    private val screenshot = BitmapFactory.decodeStream(context.contentResolver.openInputStream(cropBundle.screenshotUri))
    private val insetCropDrawable = InsetDrawable(BitmapDrawable(resources, cropBundle.crop), 0, cropBundle.topOffset, 0, cropBundle.bottomOffset)

    private var displayingScreenshot = true

    init {
        setImageBitmap(screenshot)
        setOnClickListener {
            if (displayingScreenshot)
                setImageBitmap(screenshot)
            else
                setImageDrawable(insetCropDrawable)

            displayingScreenshot = !displayingScreenshot
        }

    }
}
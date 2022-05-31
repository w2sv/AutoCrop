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
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerViewModel
import com.autocrop.activities.examination.fragments.viewpager.transitionName
import com.autocrop.activities.examination.fragments.viewpager.views.ViewPagerViewModelRetriever
import com.autocrop.collections.CropBundle
import com.autocrop.uicontroller.ViewModelRetriever

class ComparisonImageView(context: Context, attributeSet: AttributeSet):
    AppCompatImageView(context, attributeSet),
    ViewModelRetriever<ViewPagerViewModel> by ViewPagerViewModelRetriever(context){

    // TODO: move into ViewModel
    private val cropBundle: CropBundle = sharedViewModel.dataSet.currentCropBundle
    private val screenshot: Bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(cropBundle.screenshotUri))

    init {
        ViewCompat.setTransitionName(this, cropBundle.transitionName())

        var displayingScreenshot = true
        val insetCrop = InsetDrawable(BitmapDrawable(resources, cropBundle.crop), 0, cropBundle.topOffset, 0, cropBundle.bottomOffset)

        setOnClickListener {
            if (displayingScreenshot)
                setImageDrawable(insetCrop)
            else
                showScreenshot()

            displayingScreenshot = !displayingScreenshot
        }
    }

    fun prepareExitTransition(){
        showMarginalizedCrop()
    }

    private val marginalizedCropLayoutParams: RelativeLayout.LayoutParams by lazy {
        (layoutParams as RelativeLayout.LayoutParams).apply {
            setMargins(0, cropBundle.topOffset, 0, cropBundle.bottomOffset)
        }
    }

    fun showMarginalizedCrop(){
        layoutParams = marginalizedCropLayoutParams
        setImageBitmap(cropBundle.crop)
    }

    fun showScreenshot(){
        setImageBitmap(screenshot)
    }
}
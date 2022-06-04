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
import androidx.lifecycle.ViewModelStoreOwner
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerViewModel
import com.autocrop.activities.examination.fragments.viewpager.transitionName
import com.autocrop.collections.CropBundle
import com.autocrop.retriever.viewmodel.ViewModelRetriever
import com.autocrop.retriever.activity.ActivityRetriever
import com.autocrop.retriever.activity.ContextBasedActivityRetriever
import com.autocrop.utilsandroid.mutableLiveData

class ComparisonImageView(context: Context, attributeSet: AttributeSet):
    AppCompatImageView(context, attributeSet),
    ActivityRetriever<ExaminationActivity> by ContextBasedActivityRetriever(context),
    ViewModelRetriever<ComparisonViewModel> by ComparisonViewModelRetriever(context) {

    private val cropBundle: CropBundle = ViewModelProvider(activity as ViewModelStoreOwner)[ViewPagerViewModel::class.java].dataSet.currentCropBundle
    private val screenshot: Bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(cropBundle.screenshot.uri))
    private val insetCropDrawable = InsetDrawable(
        BitmapDrawable(resources, cropBundle.crop.bitmap),
        0,
        cropBundle.crop.rect.top,
        0,
        cropBundle.bottomOffset
    )

    init {
        ViewCompat.setTransitionName(this, cropBundle.transitionName())

        setOnClickListener {
            sharedViewModel.displayScreenshot.value?.let {
                sharedViewModel.displayScreenshot.mutableLiveData.postValue(!it)
            } ?: sharedViewModel.displayScreenshot.mutableLiveData.postValue(false)
        }
    }

    fun onEnterTransitionEnd(rootLayoutParams: RelativeLayout.LayoutParams){
        showMarginalizedCrop()

        layoutParams = rootLayoutParams
        showScreenshot()
    }

    fun prepareExitTransition() =
        showMarginalizedCrop()

    private val marginalizedCropLayoutParams: RelativeLayout.LayoutParams by lazy {
        (layoutParams as RelativeLayout.LayoutParams).apply {
            setMargins(
                0,
                cropBundle.crop.rect.top,
                0,
                cropBundle.bottomOffset
            )
        }
    }

    private fun showMarginalizedCrop(){
        layoutParams = marginalizedCropLayoutParams
        setImageBitmap(cropBundle.crop.bitmap)
    }

    fun update(displayScreenshot: Boolean){
        if (displayScreenshot)
            showScreenshot()
        else
            showInsetCrop()
    }

    private fun showScreenshot(){
        setImageBitmap(screenshot)
    }

    private fun showInsetCrop(){
        setImageDrawable(insetCropDrawable)
    }
}
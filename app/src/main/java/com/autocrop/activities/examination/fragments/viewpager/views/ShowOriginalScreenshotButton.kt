package com.autocrop.activities.examination.fragments.viewpager.views

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatButton
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.fragments.comparison.ShowOriginalScreenshotFragment
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerFragment
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerViewModel
import com.autocrop.uicontroller.ViewModelRetriever
import com.autocrop.uielements.view.ActivityRetriever
import com.autocrop.uielements.view.ContextBasedActivityRetriever
import com.w2sv.autocrop.R

class ShowOriginalScreenshotButton(context: Context, attributeSet: AttributeSet):
    AppCompatButton(context, attributeSet),
    ViewModelRetriever<ViewPagerViewModel> by ViewPagerViewModelRetriever(context),
    ActivityRetriever<ExaminationActivity> by ContextBasedActivityRetriever(context) {

    init {
        setOnClickListener {
            val cropImageView = activity.findViewById<ImageView>(R.id.image_view_examination_view_pager)

            typedActivity.replaceCurrentFragmentWith(
                ShowOriginalScreenshotFragment(),
                addToBackStack = true
            ){ fragmentTransaction ->
                fragmentTransaction.addSharedElement(
                    cropImageView,
                    cropImageView.transitionName
                )
            }
        }
    }
}
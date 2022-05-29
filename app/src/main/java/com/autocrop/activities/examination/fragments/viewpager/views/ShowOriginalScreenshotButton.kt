package com.autocrop.activities.examination.fragments.viewpager.views

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import com.autocrop.activities.examination.ExaminationActivity
import com.autocrop.activities.examination.fragments.comparison.ShowOriginalScreenshotFragment
import com.autocrop.activities.examination.fragments.viewpager.ViewPagerViewModel
import com.autocrop.uicontroller.ViewModelRetriever
import com.autocrop.uielements.view.ActivityRetriever
import com.autocrop.uielements.view.ContextBasedActivityRetriever

class ShowOriginalScreenshotButton(context: Context, attributeSet: AttributeSet):
    AppCompatButton(context, attributeSet),
    ViewModelRetriever<ViewPagerViewModel> by ViewPagerViewModelRetriever(context),
    ActivityRetriever<ExaminationActivity> by ContextBasedActivityRetriever(context) {

    init {
        setOnClickListener {

            typedActivity.replaceCurrentFragmentWith(
                ShowOriginalScreenshotFragment()
            )
        }
    }
}
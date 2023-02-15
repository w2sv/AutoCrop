package com.w2sv.autocrop.activities.examination.fragments.comparison.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.w2sv.androidutils.extensions.postValue
import com.w2sv.androidutils.extensions.viewModel
import com.w2sv.autocrop.activities.examination.fragments.comparison.ComparisonFragment

class ScreenshotImageView(context: Context, attributeSet: AttributeSet) :
    AppCompatImageView(context, attributeSet) {

    private val viewModel by viewModel<ComparisonFragment.ViewModel>()

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        viewModel.screenshotViewMatrixLive.postValue(imageMatrix)
    }
}
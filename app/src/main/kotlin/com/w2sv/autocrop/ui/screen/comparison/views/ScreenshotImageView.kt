package com.w2sv.autocrop.ui.screen.comparison.views

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.w2sv.androidutils.view.viewModel
import com.w2sv.autocrop.ui.screen.comparison.ComparisonViewModel

class ScreenshotImageView(context: Context, attributeSet: AttributeSet) :
    AppCompatImageView(context, attributeSet) {

    private val viewModel by viewModel<ComparisonViewModel>()

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        viewModel.postScreenshotViewImageMatrix(imageMatrix)
    }
}
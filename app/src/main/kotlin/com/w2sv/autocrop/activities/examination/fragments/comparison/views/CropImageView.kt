package com.w2sv.autocrop.activities.examination.fragments.comparison.views

import android.content.Context
import android.graphics.Matrix
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions.getScaleY
import com.w2sv.cropbundle.cropping.CropEdges

class CropImageView(context: Context, attributeSet: AttributeSet) :
    AppCompatImageView(context, attributeSet) {

    fun alignWithScreenshotImageView(screenshotImageMatrix: Matrix, cropEdges: CropEdges) {
        imageMatrix = screenshotImageMatrix
        translationY = cropEdges.top.toFloat() * screenshotImageMatrix.getScaleY()
        postInvalidate()
    }
}
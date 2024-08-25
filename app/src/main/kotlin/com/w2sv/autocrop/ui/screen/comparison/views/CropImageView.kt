package com.w2sv.autocrop.ui.screen.comparison.views

import android.content.Context
import android.graphics.Matrix
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.w2sv.autocrop.activities.examination.adjustment.extensions.getScaleY
import com.w2sv.cropbundle.cropping.model.CropEdges

class CropImageView(context: Context, attributeSet: AttributeSet) :
    AppCompatImageView(context, attributeSet) {

    fun alignWithScreenshotIV(screenshotIVMatrix: Matrix, cropEdges: CropEdges) {
        imageMatrix = screenshotIVMatrix
        translationY = cropEdges.top.toFloat() * screenshotIVMatrix.getScaleY()
        postInvalidate()
    }
}
package com.lyrebirdstudio.croppylib.util.model

import android.graphics.RectF
import androidx.annotation.Keep

@Keep
class AnimatableRectF : RectF() {

    fun setTop(top: Float) {
        this.top = top
    }

    fun setBottom(bottom: Float) {
        this.bottom = bottom
    }

    fun setRight(right: Float) {
        this.right = right
    }

    fun setLeft(left: Float) {
        this.left = left
    }
}
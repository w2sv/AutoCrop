package com.w2sv.autocrop.ui.views

import android.graphics.Rect
import android.view.TouchDelegate
import android.view.View

fun View.increaseTouchArea(pixels: Int) {
    post {
        (parent as View).touchDelegate = TouchDelegate(
            Rect().apply {
                getHitRect(this)
                top -= pixels
                bottom += pixels
                left -= pixels
                right += pixels
            },
            this
        )
    }
}
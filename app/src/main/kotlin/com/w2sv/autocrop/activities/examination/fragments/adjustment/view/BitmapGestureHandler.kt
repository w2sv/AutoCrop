package com.w2sv.autocrop.activities.examination.fragments.adjustment.view

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent

class BitmapGestureHandler(
    context: Context,
    private val onScroll: (distanceX: Float, distanceY: Float) -> Unit
) {

    fun onTouchEvent(motionEvent: MotionEvent): Boolean =
        gestureDetector.onTouchEvent(motionEvent)

    private val gestureDetector = GestureDetector(
        context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                onScroll(distanceX, distanceY)
                return true
            }
        }
    )
}
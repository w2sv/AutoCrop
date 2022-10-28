package com.lyrebirdstudio.croppylib.fragment.view

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent

class BitmapGestureHandler(
    context: Context,
    private val bitmapGestureListener: BitmapGestureListener
) {

    interface BitmapGestureListener {
        fun onScroll(distanceX: Float, distanceY: Float)
    }

    private val scrollDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            bitmapGestureListener.onScroll(distanceX, distanceY)
            return true
        }
    })

    fun onTouchEvent(motionEvent: MotionEvent): Boolean =
        scrollDetector.onTouchEvent(motionEvent)
}
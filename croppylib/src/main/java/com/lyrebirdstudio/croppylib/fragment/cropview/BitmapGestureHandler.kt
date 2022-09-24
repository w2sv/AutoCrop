package com.lyrebirdstudio.croppylib.fragment.cropview

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector

class BitmapGestureHandler(
    context: Context,
    private val bitmapGestureListener: BitmapGestureListener) {

    interface BitmapGestureListener {
        fun onScale(scaleFactor: Float, focusX: Float, focusY: Float)
        fun onScroll(distanceX: Float, distanceY: Float)
        fun onDoubleTap(motionEvent: MotionEvent)
    }

    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            bitmapGestureListener.onScale(detector.scaleFactor, detector.focusX, detector.focusY)
            return true
        }
    })
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
    private val doubleTapDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(event: MotionEvent): Boolean {
            bitmapGestureListener.onDoubleTap(event)
            return true
        }
    })

    fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        val scale = scaleDetector.onTouchEvent(motionEvent)
        val scroll = scrollDetector.onTouchEvent(motionEvent)
//        val doubleTap = doubleTapDetector.onTouchEvent(motionEvent)

        return scale || scroll
    }
}
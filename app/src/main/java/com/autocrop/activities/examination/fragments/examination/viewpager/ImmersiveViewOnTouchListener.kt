package com.autocrop.activities.examination.fragments.examination.viewpager

import android.annotation.SuppressLint
import android.graphics.Point
import android.view.MotionEvent
import android.view.View
import com.autocrop.utils.manhattanNorm
import timber.log.Timber


abstract class ImmersiveViewOnTouchListener: View.OnTouchListener {
    companion object{
        private const val CLICK_IDENTIFICATION_THRESHOLD: Int = 100
    }

    private var startCoordinates: Point = Point(-1, -1)

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        Timber.i("Called onTouch")

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> startCoordinates = event.coordinates()
            MotionEvent.ACTION_UP -> if (isClick(event.coordinates()))
                onConfirmedTouch()
        }
        return true
    }

    abstract fun onConfirmedTouch()

    private fun MotionEvent.coordinates(): Point = Point(x.toInt(), y.toInt())

    private fun isClick(endCoordinates: Point): Boolean {
        return manhattanNorm(
            startCoordinates,
            endCoordinates
        ) < CLICK_IDENTIFICATION_THRESHOLD
    }
}
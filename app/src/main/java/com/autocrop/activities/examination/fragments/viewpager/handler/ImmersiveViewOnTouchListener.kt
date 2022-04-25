package com.autocrop.activities.examination.fragments.viewpager.handler

import android.annotation.SuppressLint
import android.graphics.Point
import android.view.MotionEvent
import android.view.View
import com.autocrop.utils.android.manhattanNorm
import com.autocrop.utils.logAfterwards

/**
 * Enables distinction between pulling up navigation bar and clicks
 * whilst employing immersive view app layout
 */
abstract class ImmersiveViewOnTouchListener: View.OnTouchListener {

    companion object{
        private fun isClick(touchEventStartPoint: Point, touchEventEndPoint: Point): Boolean{
            val clickIdentificationCoordinateThreshold = 100

            return manhattanNorm(touchEventStartPoint, touchEventEndPoint) < clickIdentificationCoordinateThreshold
        }
    }

    private var touchEventStartPoint: Point? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean = logAfterwards("Called onTouch") {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> touchEventStartPoint = event.point
            MotionEvent.ACTION_UP -> if (isClick(touchEventStartPoint!!, event.point)) onClick().also { touchEventStartPoint = null }
        }
        return true
    }

    private val MotionEvent.point: Point
        get() = Point(x.toInt(), y.toInt())

    abstract fun onClick()
}
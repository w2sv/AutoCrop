package com.autocrop.uielements.view

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import com.autocrop.utils.Consumable
import com.autocrop.utils.logAfterwards
import com.autocrop.utilsandroid.isClick

abstract class ImmersiveViewOnTouchListener
    : View.OnTouchListener {

    private var touchStart by Consumable<MotionEvent>()

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean = logAfterwards("Called onTouch") {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> touchStart = event
            MotionEvent.ACTION_UP -> touchStart?.let {
                if (isClick(it, event))
                    onClick()
            }
            else -> Unit
        }
        return true
    }

    abstract fun onClick()
}
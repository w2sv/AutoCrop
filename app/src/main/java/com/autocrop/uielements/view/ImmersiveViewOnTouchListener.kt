package com.autocrop.uielements.view

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import com.autocrop.utils.Consumable
import com.autocrop.utilsandroid.isClick

abstract class ImmersiveViewOnTouchListener
    : View.OnTouchListener {

    private var touchStartConsumable by Consumable<MotionEvent>()

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View?, event: MotionEvent?): Boolean{
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> touchStartConsumable = event
            MotionEvent.ACTION_UP -> touchStartConsumable?.let {
                if (isClick(it, event))
                    onClick()
            }
            else -> Unit
        }
        return true
    }

    abstract fun onClick()
}
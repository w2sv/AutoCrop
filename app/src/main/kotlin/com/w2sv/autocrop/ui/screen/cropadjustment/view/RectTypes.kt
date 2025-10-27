package com.w2sv.autocrop.ui.screen.cropadjustment.view

import android.graphics.RectF
import com.w2sv.domain.model.CropEdges
import kotlin.math.roundToInt

class BitmapSpaceRect(rect: RectF = RectF()) : RectF(rect) {
    fun cropEdges(): CropEdges =
        CropEdges(top.roundToInt(), bottom.roundToInt())
}

class ViewSpaceRect(rect: RectF = RectF()) : RectF(rect)

val RectF.bitmapSpace get() = BitmapSpaceRect(this)
val RectF.viewSpace get() = ViewSpaceRect(this)

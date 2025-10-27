package com.w2sv.autocrop.ui.screen.cropadjustment.extensions

import android.graphics.RectF
import com.w2sv.domain.model.CropEdges

fun CropEdges.rectF(width: Int): RectF =
    RectF(0F, top.toFloat(), width.toFloat(), bottom.toFloat())

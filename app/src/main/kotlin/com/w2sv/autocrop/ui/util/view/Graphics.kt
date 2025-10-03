package com.w2sv.autocrop.ui.util.view

import android.graphics.Paint
import android.graphics.Path
import com.w2sv.kotlinutils.threadUnsafeLazy

fun buildPaint(block: Paint.() -> Unit): Paint =
    Paint().apply(block)

fun threadUnsafeLazyPaint(block: Paint.() -> Unit) =
    threadUnsafeLazy { buildPaint(block) }

fun buildPath(block: Path.() -> Unit): Path =
    Path().apply(block)

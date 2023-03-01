package com.w2sv.autocrop.activities.examination.fragments.adjustment.extensions

import android.graphics.Matrix

fun FloatArray.asMappedFrom(src: FloatArray, mapMatrix: Matrix): FloatArray =
    apply {
        mapMatrix.mapPoints(
            this,
            src
        )
    }
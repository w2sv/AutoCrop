package com.lyrebirdstudio.croppylib.utils.extensions

import java.math.RoundingMode
import java.text.DecimalFormat

fun Float.rounded(nDecimalPlaces: Int) =
    DecimalFormat("#.${"#".repeat(nDecimalPlaces)}")
        .apply {
            roundingMode = RoundingMode.HALF_EVEN
        }
        .format(this)
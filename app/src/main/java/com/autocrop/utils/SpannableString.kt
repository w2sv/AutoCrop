package com.autocrop.utils

import android.text.SpannableString

fun SpannableString.setSpanHolistically(what: Any){
    setSpan(what, 0, length, 0)
}
package com.autocrop.utilsandroid

import android.content.ClipData
import android.content.Intent

fun Intent.clipDataItems(): List<ClipData.Item>? = clipData?.run {
    (0 until itemCount).map {
        getItemAt(it)
    }
}
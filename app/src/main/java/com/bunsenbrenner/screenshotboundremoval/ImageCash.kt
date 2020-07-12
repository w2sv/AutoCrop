package com.bunsenbrenner.screenshotboundremoval

import android.graphics.Bitmap
import android.net.Uri

object ImageCash{
    val cash: MutableMap<Uri, Bitmap> = mutableMapOf()
    fun clear() = cash.clear()
    fun keys(): MutableSet<Uri> = cash.keys
    fun values(): MutableCollection<Bitmap> = cash.values

    operator fun set(key: Uri, value: Bitmap){
        cash[key] = value
    }

    operator fun get(key: Uri): Bitmap? = cash[key]
}
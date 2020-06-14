package com.example.screenshotboundremoval

import android.graphics.Bitmap
import android.net.Uri

// TODO: find @property equivalent

object ImageCash{
    val cash: MutableMap<Uri, Bitmap> = mutableMapOf()
    fun clear() = cash.clear()
    fun keys(): MutableSet<Uri> = cash.keys
    fun values(): MutableCollection<Bitmap> = cash.values
}
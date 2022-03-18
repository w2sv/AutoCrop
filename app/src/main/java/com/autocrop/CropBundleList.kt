package com.autocrop

import android.graphics.Bitmap
import android.net.Uri
import com.autocrop.utils.logAfterwards


//$$$$$$$$$$$$$$$$$$
// CropBundleList $$
//$$$$$$$$$$$$$$$$$$
typealias CropBundleList = MutableList<CropBundle>

fun CropBundleList.clearAndLog() = logAfterwards("Cleared cropBundleList"){clear()}

val cropBundleList: CropBundleList = mutableListOf()


//$$$$$$$$$$$$$$
// CropBundle $$
//$$$$$$$$$$$$$$
typealias CropBundle = Triple<Uri, Bitmap, Int>

val CropBundle.screenshotUri: Uri
    get() = first
val CropBundle.crop: Bitmap
    get() = second
val CropBundle.retentionPercentage: Int
    get() = third
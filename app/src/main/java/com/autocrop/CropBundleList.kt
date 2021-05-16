package com.autocrop

import android.graphics.Bitmap
import android.net.Uri
import timber.log.Timber


typealias CropBundleList = MutableList<CropBundle>

fun CropBundleList.clearAndLog(){
    clear().also {
        Timber.i("Cleared cropBundleList")
    }
}

val cropBundleList: CropBundleList = mutableListOf()


typealias CropBundle = Triple<Uri, Bitmap, Int>

val CropBundle.screenshotUri: Uri
    get() = first
val CropBundle.crop: Bitmap
    get() = second
val CropBundle.retentionPercentage: Int
    get() = third
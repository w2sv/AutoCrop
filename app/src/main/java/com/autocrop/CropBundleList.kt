package com.autocrop

import android.graphics.Bitmap
import android.net.Uri
import timber.log.Timber


typealias CropBundle = Triple<Uri, Bitmap, Int>
val CropBundle.screenshotUri: Uri
    get() = first
val CropBundle.crop: Bitmap
    get() = second
val CropBundle.retentionPercentage: Int
    get() = third

val cropBundleList: MutableList<CropBundle> = mutableListOf()

/**
 * Conducts additional logging
 */
fun clearCropBundleList(){
    cropBundleList.clear().also {
        Timber.i("Cleared cropBundleList")
    }
}
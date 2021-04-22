package com.autocrop

import android.graphics.Bitmap
import android.net.Uri
import timber.log.Timber


typealias CropBundle = Triple<Uri, Bitmap, Int>
fun CropBundle.screenshotUri(): Uri = this.first
fun CropBundle.crop(): Bitmap = this.second
fun CropBundle.retentionPercentage(): Int = this.third


val cropBundleList: MutableList<CropBundle> = mutableListOf()

/**
 * Conducts additional logging
 */
fun clearCropBundleList(){
    cropBundleList.clear().also {
        Timber.i("Cleared cropBundleList")
    }
}
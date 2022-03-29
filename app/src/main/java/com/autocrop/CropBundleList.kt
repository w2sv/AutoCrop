package com.autocrop

import android.graphics.Bitmap
import android.net.Uri
import com.autocrop.utils.logAfterwards

//$$$$$$$$$$$$$$$$$$
// CropBundleList $$
//$$$$$$$$$$$$$$$$$$
typealias CropBundleList = MutableList<CropBundle>

//$$$$$$$$$$$$$$
// CropBundle $$
//$$$$$$$$$$$$$$
data class CropBundle(
    val screenshotUri: Uri,
    val crop: Bitmap,
    val discardedPercentage: Int,
    val approximateDiscardedFileSize: Int
)
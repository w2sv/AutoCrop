package com.autocrop.activities.cropping.cropping

import android.graphics.Bitmap
import com.autocrop.GlobalParameters
import com.autocrop.activities.cropping.cropping.manners.BitmapWithRetentionPercentage
import com.autocrop.activities.cropping.cropping.manners.continuouslyCroppedImage
import com.autocrop.activities.cropping.cropping.manners.indentedlyCroppedImage


fun croppedImage(image: Bitmap): BitmapWithRetentionPercentage?{
    return if (GlobalParameters.indentedCropping)
        indentedlyCroppedImage(image)
    else
        continuouslyCroppedImage(image)
}
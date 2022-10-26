package com.autocrop.activities.iodetermination.fragments.comparison

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.InsetDrawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.autocrop.CropBundle

class ComparisonViewModel(val cropBundle: CropBundle, val screenshotBitmap: Bitmap, cropBitmapDrawable: BitmapDrawable): ViewModel(){
    val displayScreenshot: LiveData<Boolean> by lazy {
        MutableLiveData(false)
    }
    val useInsetLayoutParams: LiveData<Boolean> by lazy {
        MutableLiveData(true)
    }

    var enterTransitionCompleted = false

    val cropInsets: Array<Int> =
        cropBundle.run {
            arrayOf(
                0,
                crop.edges.top,
                0,
                screenshot.height - crop.edges.bottom
            )
        }

    val cropInsetDrawable: InsetDrawable =
        cropInsets.run{
            InsetDrawable(
                cropBitmapDrawable,
                get(0), get(1), get(2), get(3)
            )
        }
}
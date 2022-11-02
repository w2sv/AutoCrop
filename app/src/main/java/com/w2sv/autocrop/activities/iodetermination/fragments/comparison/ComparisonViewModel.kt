package com.w2sv.autocrop.activities.iodetermination.fragments.comparison

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.InsetDrawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.w2sv.autocrop.CropBundle
import com.w2sv.kotlinutils.delegates.AutoSwitch

class ComparisonViewModel(
    val cropBundle: CropBundle,
    val screenshotBitmap: Bitmap,
    cropBitmapDrawable: BitmapDrawable
) : ViewModel() {

    class Factory(
        private val cropBundle: CropBundle,
        private val screenshotBitmap: Bitmap,
        private val cropBitmapDrawable: BitmapDrawable
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ComparisonViewModel(cropBundle, screenshotBitmap, cropBitmapDrawable) as T
    }

    val enterTransitionCompleted by AutoSwitch(false, switchOn = false)

    val displayScreenshot: LiveData<Boolean> by lazy {
        MutableLiveData(false)
    }
    val useInsetLayoutParams: LiveData<Boolean> by lazy {
        MutableLiveData(true)
    }
    val showButtons: LiveData<Boolean> by lazy {
        MutableLiveData(false)
    }

    val cropFittedInsets: Array<Int> =
        cropBundle.run {
            arrayOf(
                0,
                crop.edges.top,
                0,
                screenshot.height - crop.edges.bottom
            )
        }

    val cropInsetDrawable: InsetDrawable =
        cropFittedInsets.run {
            InsetDrawable(
                cropBitmapDrawable,
                get(0), get(1), get(2), get(3)
            )
        }
}
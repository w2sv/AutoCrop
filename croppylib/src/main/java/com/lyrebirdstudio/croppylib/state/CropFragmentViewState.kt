package com.lyrebirdstudio.croppylib.state

import android.graphics.RectF
import com.lyrebirdstudio.croppylib.main.CroppyTheme

data class CropFragmentViewState(
    val croppyTheme: CroppyTheme = CroppyTheme(),
    val height: Float? = null
) {
    fun onCropSizeChanged(cropRect: RectF): CropFragmentViewState {
        return CropFragmentViewState(
            croppyTheme = croppyTheme,
            height = cropRect.height()
        )
    }

    fun onThemeChanged(croppyTheme: CroppyTheme): CropFragmentViewState {
        return CropFragmentViewState(
            croppyTheme = croppyTheme,
            height = height
        )
    }
}
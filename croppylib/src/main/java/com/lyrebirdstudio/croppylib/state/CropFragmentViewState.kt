package com.lyrebirdstudio.croppylib.state

import android.content.Context
import android.graphics.RectF
import android.text.Spannable
import android.text.SpannableString
import com.lyrebirdstudio.croppylib.inputview.SizeInputData
import com.lyrebirdstudio.croppylib.inputview.SizeInputViewType
import kotlin.math.roundToInt
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.core.content.ContextCompat
import com.lyrebirdstudio.croppylib.R
import com.lyrebirdstudio.croppylib.main.CroppyTheme

data class CropFragmentViewState(
    val croppyTheme: CroppyTheme = CroppyTheme.default(),
    val sizeInputData: SizeInputData? = null
) {

    fun getHeightButtonText(context: Context): Spannable {
        if (sizeInputData?.heightValue?.isNaN() == true)
            return SpannableString("")

        return SpannableString("H ${sizeInputData?.heightValue?.roundToInt()}").apply {
            setSpan(
                ForegroundColorSpan(ContextCompat.getColor(context, croppyTheme.accentColor)),
                0,
                1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            setSpan(
                ForegroundColorSpan(ContextCompat.getColor(context, R.color.white)),
                1,
                length - 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    fun onCropSizeChanged(cropRect: RectF): CropFragmentViewState {
        return CropFragmentViewState(
            sizeInputData = SizeInputData(
                type = SizeInputViewType.WIDTH,
                widthValue = cropRect.width(),
                heightValue = cropRect.height()
            ),
            croppyTheme = croppyTheme
        )
    }

    fun onThemeChanged(croppyTheme: CroppyTheme): CropFragmentViewState {
        return CropFragmentViewState(
            sizeInputData = sizeInputData,
            croppyTheme = croppyTheme
        )
    }
}
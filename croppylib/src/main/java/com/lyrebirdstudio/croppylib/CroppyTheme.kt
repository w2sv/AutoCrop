package com.lyrebirdstudio.croppylib

import android.os.Parcelable
import androidx.annotation.ColorRes
import kotlinx.parcelize.Parcelize

@Parcelize
data class CroppyTheme(@ColorRes val accentColor: Int = R.color.blue) : Parcelable
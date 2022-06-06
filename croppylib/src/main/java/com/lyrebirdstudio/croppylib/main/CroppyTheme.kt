package com.lyrebirdstudio.croppylib.main

import android.os.Parcelable
import androidx.annotation.ColorRes
import com.lyrebirdstudio.croppylib.R
import kotlinx.parcelize.Parcelize

@Parcelize
data class CroppyTheme(@ColorRes val accentColor: Int = R.color.blue) : Parcelable
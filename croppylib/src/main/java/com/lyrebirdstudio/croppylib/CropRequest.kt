package com.lyrebirdstudio.croppylib

import android.content.Context
import android.graphics.Rect
import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
open class CropRequest(
    open val sourceUri: Uri,
    open val requestCode: Int,
    open val initialCropRect: Rect,
    open val croppyTheme: CroppyTheme,
    open val exitActivityAnimation: ((Context) -> Unit)?
    ) : Parcelable



package com.lyrebirdstudio.croppylib.main

import android.content.Context
import android.graphics.Rect
import android.net.Uri
import android.os.Parcelable
import com.lyrebirdstudio.croppylib.R
import kotlinx.android.parcel.Parcelize

@Parcelize
open class CropRequest(
    open val sourceUri: Uri,
    open val requestCode: Int,
    open val initialCropRect: Rect?,
    open val croppyTheme: CroppyTheme,
    open val exitActivityAnimation: ((Context) -> Unit)?
    ) : Parcelable {

    @Parcelize
    class Manual(
        override val sourceUri: Uri,
        val destinationUri: Uri,
        override val requestCode: Int,
        override val initialCropRect: Rect,
        override val croppyTheme: CroppyTheme = CroppyTheme(R.color.blue),
        override val exitActivityAnimation: ((Context) -> Unit)? = null
    ) : CropRequest(sourceUri, requestCode, initialCropRect, croppyTheme, exitActivityAnimation)

    @Parcelize
    class Auto(
        override val sourceUri: Uri,
        override val requestCode: Int,
        override val initialCropRect: Rect,
        val storageType: StorageType = StorageType.EXTERNAL,
        override val croppyTheme: CroppyTheme = CroppyTheme(R.color.blue),
        override val exitActivityAnimation: ((Context) -> Unit)? = null
    ) : CropRequest(sourceUri, requestCode, initialCropRect, croppyTheme, exitActivityAnimation)

    companion object {
        fun empty(): CropRequest =
            CropRequest(Uri.EMPTY, -1, null, CroppyTheme(R.color.blue), null)
    }
}



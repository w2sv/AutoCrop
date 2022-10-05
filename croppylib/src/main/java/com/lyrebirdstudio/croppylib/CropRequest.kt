package com.lyrebirdstudio.croppylib

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
open class CropRequest(
    val uri: Uri,
    val initialCropEdges: Pair<Int, Int>,
    val cropEdgePairCandidates: List<Pair<Int, Int>>,
    val croppyTheme: CroppyTheme,
    val exitActivityAnimation: ((Context) -> Unit)?) : Parcelable
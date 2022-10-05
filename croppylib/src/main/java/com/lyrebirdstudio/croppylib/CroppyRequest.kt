package com.lyrebirdstudio.croppylib

import android.content.Context
import android.net.Uri
import android.os.Parcelable
import com.lyrebirdstudio.croppylib.fragment.cropview.CropEdges
import kotlinx.parcelize.Parcelize

@Parcelize
class CroppyRequest(
    val uri: Uri,
    val initialCropEdges: CropEdges,
    val cropEdgePairCandidates: List<CropEdges>,
    val croppyTheme: CroppyTheme,
    val exitActivityAnimation: ((Context) -> Unit)?) : Parcelable
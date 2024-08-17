package com.w2sv.cropbundle.io

import com.w2sv.cropbundle.io.utils.extensionLessFileName
import com.w2sv.kotlinutils.time.dateTimeNow

fun cropFileName(fileName: String, mimeType: ImageMimeType): String =
    "${extensionLessFileName(fileName)}-${CROP_FILE_ADDENDUM}_${dateTimeNow()}.${mimeType.outFileExtension}"

const val CROP_FILE_ADDENDUM = "AutoCropped"
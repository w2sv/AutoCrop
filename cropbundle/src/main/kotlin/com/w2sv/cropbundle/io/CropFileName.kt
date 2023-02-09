package com.w2sv.cropbundle.io

import com.w2sv.cropbundle.io.utils.extensionLessFileName
import com.w2sv.kotlinutils.dateTimeNow

fun cropFileName(fileName: String, mimeType: ImageMimeType): String =
    "${extensionLessFileName(fileName)}-${CROP_FILE_ADDENDUM}_${dateTimeNow()}.${mimeType.fileExtension}"

const val CROP_FILE_ADDENDUM = "AutoCropped"
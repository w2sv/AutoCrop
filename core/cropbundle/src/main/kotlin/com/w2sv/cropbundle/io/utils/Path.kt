package com.w2sv.cropbundle.io.utils

internal fun extensionLessFileName(fileName: String): String =
    fileName.replaceAfterLast(".", "")
        .run { slice(0 until lastIndex) }
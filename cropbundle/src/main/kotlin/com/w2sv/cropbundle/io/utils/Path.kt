package com.w2sv.cropbundle.io.utils

fun extensionLessFileName(fileName: String): String =
    fileName.replaceAfterLast(".", "")
        .run { slice(0 until lastIndex) }
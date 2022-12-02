package com.w2sv.autocrop.cropbundle.io.utils

//fun pathTail(path: String): String =
//    "/${path.split("/").takeLast(2).joinToString("/")}"

fun extensionLessFileName(fileName: String): String =
    fileName.replaceAfterLast(".", "")
        .run { slice(0 until lastIndex) }
package com.autocrop.utils.kotlin.extensions

fun ByteArray.writeInt(offset: Int, data: Int) {
    this[offset + 0] = (data shr 0).toByte()
    this[offset + 1] = (data shr 8).toByte()
    this[offset + 2] = (data shr 16).toByte()
    this[offset + 3] = (data shr 24).toByte()
}

fun ByteArray.readInt(offset: Int): Int =
    (this[offset + 3].toInt() shl 24) or
    (this[offset + 2].toInt() and 0xff shl 16) or
    (this[offset + 1].toInt() and 0xff shl 8) or
    (this[offset + 0].toInt() and 0xff)
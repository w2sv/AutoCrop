package com.autocrop.collections

data class CropIOResults(
    val nSavedCrops: Int,
    val nDeletedScreenshots: Int,
    val cropWriteDirIdentifier: String) {

    constructor(byteArray: ByteArray): this(
        byteArray[0].toInt(),
        byteArray[1].toInt(),
        byteArray.slice(2 until byteArray.size).toByteArray().toString(Charsets.UTF_8)
    )

    fun toByteArray(): ByteArray =
        byteArrayOf(
            nSavedCrops.toByte(),
            nDeletedScreenshots.toByte()
        ) + cropWriteDirIdentifier.toByteArray(Charsets.UTF_8)
}
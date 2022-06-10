package com.autocrop.collections

/**
 * Enables transfer of respective data via Intent through encoding to & decoding
 * from ByteArray
 */
data class ImageFileIOSynopsis(
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
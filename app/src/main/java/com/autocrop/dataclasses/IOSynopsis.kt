package com.autocrop.dataclasses

/**
 * Enables transfer of respective data via Intent through encoding to & decoding
 * from [ByteArray]
 */
data class IOSynopsis(
    val nSavedCrops: Int,
    val nDeletedScreenshots: Int,
    val cropWriteDirIdentifier: String) {

    companion object{
        fun fromByteArray(byteArray: ByteArray): IOSynopsis =
            IOSynopsis(
                byteArray.readInt(0),
                byteArray.readInt(4),
                byteArray
                    .slice(8 until byteArray.size)
                    .toByteArray()
                    .toString(Charsets.UTF_8)
            )
    }

    fun toByteArray(): ByteArray =
        ByteArray(8).apply {
            writeInt(0, nSavedCrops)
            writeInt(4, nDeletedScreenshots)
        } + cropWriteDirIdentifier.toByteArray(Charsets.UTF_8)
}

private fun ByteArray.writeInt(offset: Int, data: Int){
    this[offset + 0] = (data shr 0).toByte()
    this[offset + 1] = (data shr 8).toByte()
    this[offset + 2] = (data shr 16).toByte()
    this[offset + 3] = (data shr 24).toByte()
}

private fun ByteArray.readInt(offset: Int): Int =
    (this[offset + 3].toInt() shl 24) or
    (this[offset + 2].toInt() and 0xff shl 16) or
    (this[offset + 1].toInt() and 0xff shl 8) or
    (this[offset + 0].toInt() and 0xff)

package com.autocrop.dataclasses

/**
 * Enables transfer of respective data via Intent through encoding to & decoding
 * from ByteArray
 */
data class IOSynopsis(
    val nSavedCrops: Int,
    val nDeletedScreenshots: Int,
    val cropWriteDirIdentifier: String) {

    companion object{
        fun fromByteArray(byteArray: ByteArray): IOSynopsis =
            byteArray.iterator().run {
                IOSynopsis(
                    nextByte().toInt(),
                    nextByte().toInt(),
                    asSequence().toList().toByteArray().toString(Charsets.UTF_8)
                )
            }
    }

    fun toByteArray(): ByteArray =
        byteArrayOf(
            nSavedCrops.toByte(),
            nDeletedScreenshots.toByte(),
            *cropWriteDirIdentifier.toByteArray(Charsets.UTF_8)
        )
}
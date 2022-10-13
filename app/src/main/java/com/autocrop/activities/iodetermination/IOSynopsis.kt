package com.autocrop.activities.iodetermination

import com.autocrop.utils.kotlin.extensions.readInt
import com.autocrop.utils.kotlin.extensions.writeInt

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

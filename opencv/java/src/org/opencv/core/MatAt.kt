@file:OptIn(ExperimentalUnsignedTypes::class)

package org.opencv.core

import org.opencv.core.Mat.*

fun Mat.get(row: Int, col: Int, data: UByteArray) =
    this.get(row, col, data.asByteArray())

fun Mat.get(indices: IntArray, data: UByteArray) =
    this.get(indices, data.asByteArray())

fun Mat.put(row: Int, col: Int, data: UByteArray) =
    this.put(row, col, data.asByteArray())

fun Mat.put(indices: IntArray, data: UByteArray) =
    this.put(indices, data.asByteArray())

fun Mat.get(row: Int, col: Int, data: UShortArray) =
    this.get(row, col, data.asShortArray())

fun Mat.get(indices: IntArray, data: UShortArray) =
    this.get(indices, data.asShortArray())

fun Mat.put(row: Int, col: Int, data: UShortArray) =
    this.put(row, col, data.asShortArray())

fun Mat.put(indices: IntArray, data: UShortArray) =
    this.put(indices, data.asShortArray())

/***
 *  Example use:
 *
 *  val (b, g, r) = mat.at<UByte>(50, 50).v3c
 *  mat.at<UByte>(50, 50).val = T3(245u, 113u, 34u)
 *
 */
@Suppress("UNCHECKED_CAST")
inline fun <reified T> Mat.at(row: Int, col: Int): Atable<T> =
    when (T::class) {
        Byte::class, Double::class, Float::class, Int::class, Short::class -> this.at(
            T::class.java,
            row,
            col
        )

        UByte::class -> AtableUByte(this, row, col) as Atable<T>
        UShort::class -> AtableUShort(this, row, col) as Atable<T>
        else -> throw RuntimeException("Unsupported class type")
    }

@Suppress("UNCHECKED_CAST")
inline fun <reified T> Mat.at(idx: IntArray): Atable<T> =
    when (T::class) {
        Byte::class, Double::class, Float::class, Int::class, Short::class -> this.at(
            T::class.java,
            idx
        )

        UByte::class -> AtableUByte(this, idx) as Atable<T>
        UShort::class -> AtableUShort(this, idx) as Atable<T>
        else -> throw RuntimeException("Unsupported class type")
    }

class AtableUByte(val mat: Mat, val indices: IntArray) : Atable<UByte> {

    constructor(mat: Mat, row: Int, col: Int) : this(mat, intArrayOf(row, col))

    override fun getV(): UByte {
        val data = UByteArray(1)
        mat.get(indices, data)
        return data[0]
    }

    override fun setV(v: UByte) {
        val data = ubyteArrayOf(v)
        mat.put(indices, data)
    }

    override fun getV2c(): Tuple2<UByte> {
        val data = UByteArray(2)
        mat.get(indices, data)
        return Tuple2(data[0], data[1])
    }

    override fun setV2c(v: Tuple2<UByte>) {
        val data = ubyteArrayOf(v._0, v._1)
        mat.put(indices, data)
    }

    override fun getV3c(): Tuple3<UByte> {
        val data = UByteArray(3)
        mat.get(indices, data)
        return Tuple3(data[0], data[1], data[2])
    }

    override fun setV3c(v: Tuple3<UByte>) {
        val data = ubyteArrayOf(v._0, v._1, v._2)
        mat.put(indices, data)
    }

    override fun getV4c(): Tuple4<UByte> {
        val data = UByteArray(4)
        mat.get(indices, data)
        return Tuple4(data[0], data[1], data[2], data[3])
    }

    override fun setV4c(v: Tuple4<UByte>) {
        val data = ubyteArrayOf(v._0, v._1, v._2, v._3)
        mat.put(indices, data)
    }
}

class AtableUShort(val mat: Mat, val indices: IntArray) : Atable<UShort> {

    constructor(mat: Mat, row: Int, col: Int) : this(mat, intArrayOf(row, col))

    override fun getV(): UShort {
        val data = UShortArray(1)
        mat.get(indices, data)
        return data[0]
    }

    override fun setV(v: UShort) {
        val data = ushortArrayOf(v)
        mat.put(indices, data)
    }

    override fun getV2c(): Tuple2<UShort> {
        val data = UShortArray(2)
        mat.get(indices, data)
        return Tuple2(data[0], data[1])
    }

    override fun setV2c(v: Tuple2<UShort>) {
        val data = ushortArrayOf(v._0, v._1)
        mat.put(indices, data)
    }

    override fun getV3c(): Tuple3<UShort> {
        val data = UShortArray(3)
        mat.get(indices, data)
        return Tuple3(data[0], data[1], data[2])
    }

    override fun setV3c(v: Tuple3<UShort>) {
        val data = ushortArrayOf(v._0, v._1, v._2)
        mat.put(indices, data)
    }

    override fun getV4c(): Tuple4<UShort> {
        val data = UShortArray(4)
        mat.get(indices, data)
        return Tuple4(data[0], data[1], data[2], data[3])
    }

    override fun setV4c(v: Tuple4<UShort>) {
        val data = ushortArrayOf(v._0, v._1, v._2, v._3)
        mat.put(indices, data)
    }
}

operator fun <T> Tuple2<T>.component1(): T =
    this._0

operator fun <T> Tuple2<T>.component2(): T =
    this._1

operator fun <T> Tuple3<T>.component1(): T =
    this._0

operator fun <T> Tuple3<T>.component2(): T =
    this._1

operator fun <T> Tuple3<T>.component3(): T =
    this._2

operator fun <T> Tuple4<T>.component1(): T =
    this._0

operator fun <T> Tuple4<T>.component2(): T =
    this._1

operator fun <T> Tuple4<T>.component3(): T =
    this._2

operator fun <T> Tuple4<T>.component4(): T =
    this._3

fun <T> T2(_0: T, _1: T): Tuple2<T> =
    Tuple2(_0, _1)

fun <T> T3(_0: T, _1: T, _2: T): Tuple3<T> =
    Tuple3(_0, _1, _2)

fun <T> T4(_0: T, _1: T, _2: T, _3: T): Tuple4<T> =
    Tuple4(_0, _1, _2, _3)

package com.w2sv.autocrop.screenshotlistening.notifications

import slimber.log.i
import java.util.PriorityQueue

open class UniqueGroupedIds(baseSeed: Int) : PriorityQueue<Int>() {

    companion object {
        fun idBase(seed: Int): Int =
            seed * 100
    }

    private val idBase: Int = idBase(baseSeed)

    fun getNewId(): Int =
        lastOrNull()?.let { it + 1 }
            ?: idBase

    fun getAndAddNewId(): Int =
        getNewId()
            .also {
                add(it)
                i { "Added pendingRequestCode $it" }
            }

    fun getAndAddMultipleNewIds(n: Int): ArrayList<Int> =
        ArrayList((0 until n)
            .map { getAndAddNewId() })
}
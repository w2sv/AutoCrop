package com.w2sv.screenshotlistening.notifications

import java.util.PriorityQueue
import slimber.log.i

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
        ArrayList(
            (0 until n)
                .map { getAndAddNewId() }
        )
}

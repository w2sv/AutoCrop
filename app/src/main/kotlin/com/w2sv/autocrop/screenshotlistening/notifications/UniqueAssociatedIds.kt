package com.w2sv.autocrop.screenshotlistening.notifications

import java.util.PriorityQueue

open class UniqueAssociatedIds(baseSeed: Int) : PriorityQueue<Int>() {

    companion object{
        fun idBase(seed: Int): Int =
            seed * 100
    }

    private val idBase: Int = idBase(baseSeed)

    fun getNewId(): Int =
        lastOrNull()?.let { it + 1 }
            ?: idBase
}
package com.w2sv.autocrop.screenshotlistening.notifications

import slimber.log.i
import java.util.PriorityQueue

class PendingIntentRequestCodes(baseSeed: Int) : PriorityQueue<Int>() {

    companion object{
        fun uniqueIdBase(seed: Int): Int =
            seed * 100
    }

    private val base = uniqueIdBase(baseSeed)

    fun makeAndAdd(): Int {
        val newRequestCode = lastOrNull()?.let { it + 1 }
            ?: base
        add(newRequestCode)
        i { "Added pendingRequestCode $newRequestCode" }
        return newRequestCode
    }

    fun makeAndAddMultiple(n: Int): ArrayList<Int> =
        ArrayList((0 until n)
            .map { makeAndAdd() })
}
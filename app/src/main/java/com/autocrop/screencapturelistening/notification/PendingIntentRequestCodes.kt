package com.autocrop.screencapturelistening.notification

import timber.log.Timber
import java.util.PriorityQueue

class PendingIntentRequestCodes(private val base: Int): PriorityQueue<Int>(){
    fun makeAndAdd(): Int{
        val newRequestCode = lastOrNull()?.let { it + 1 } ?: base
        add(newRequestCode)
        Timber.i("Added pendingRequestCode $newRequestCode")
        return newRequestCode
    }

    fun makeAndAddMultiple(n: Int): ArrayList<Int> =
        ArrayList((0 until n).map { makeAndAdd() })
}
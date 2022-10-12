package com.autocrop.screencapturelistening.notification

import timber.log.Timber
import java.util.PriorityQueue

class ScopeWideUniqueIds(private val idBase: Int): PriorityQueue<Int>(){
    fun addNewId(): Int{
        val newId = lastOrNull()?.let { it + 1 } ?: idBase
        add(newId)
        Timber.i("Added pendingRequestCode $newId")
        return newId
    }
}
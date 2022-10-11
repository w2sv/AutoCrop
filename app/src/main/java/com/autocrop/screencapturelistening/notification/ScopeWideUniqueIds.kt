package com.autocrop.screencapturelistening.notification

import java.util.PriorityQueue

class ScopeWideUniqueIds: PriorityQueue<Int>(){
    fun addNewId(): Int =
        lastOrNull()?.let { it + 1 } ?: 0
            .also { add(it) }
}
package com.autocrop.screencapturelistening.notification

import java.util.PriorityQueue

class ScopeWideUniqueIds: PriorityQueue<Int>(){
    fun addNewId(): Int{
        val newId = lastOrNull()?.let { it + 1 } ?: 0
        add(newId)
        return newId
    }
}
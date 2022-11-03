package com.w2sv.bidirectionalviewpager.livedata

import androidx.lifecycle.LiveData

open class MutableListLiveData<T>(private val delegator: MutableList<T>) :
    LiveData<List<T>>(delegator),
    MutableList<T> by delegator {

    private fun post() {
        postValue(this)
    }

    override fun removeAt(index: Int): T =
        delegator.removeAt(index)
            .also { post() }

    override fun add(element: T): Boolean =
        delegator.add(element)
            .also { post() }
}
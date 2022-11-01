package com.w2sv.bidirectionalviewpager.livedata

import androidx.lifecycle.LiveData

abstract class MutableListLiveData<T>(private val delegator: MutableList<T>) :
    LiveData<MutableList<T>>(delegator),
    MutableList<T> by delegator {

    private fun post() {
        postValue(this)
    }

    override fun removeAt(index: Int): T =
        delegator.removeAt(index)
            .also { post() }
}
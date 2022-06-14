package com.autocrop.utilsandroid.livedata

import androidx.lifecycle.LiveData

abstract class MutableListLiveData<T>(private val delegator: MutableList<T>):
    LiveData<MutableList<T>>(delegator),
    MutableList<T> by delegator{

    private fun postValue(){
        postValue(this)
    }

    override fun removeAt(index: Int): T =
        delegator.removeAt(index)
//            .also { postValue() }
}
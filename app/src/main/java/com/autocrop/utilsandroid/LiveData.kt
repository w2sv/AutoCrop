package com.autocrop.utilsandroid

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

val <T> LiveData<T>.mutableLiveData: MutableLiveData<T>
    get() = this as MutableLiveData<T>

abstract class MutableListLiveData<T>(private val delegator: MutableList<T>):
    LiveData<MutableList<T>>(delegator),
    MutableList<T> by delegator{

    private fun postValue(){
        postValue(this)
    }

    override fun removeAt(index: Int): T =
        delegator.removeAt(index)
            .also { postValue() }
}
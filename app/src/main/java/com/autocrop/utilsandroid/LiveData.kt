package com.autocrop.utilsandroid

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

val <T> LiveData<T>.asMutable: MutableLiveData<T>
    get() = this as MutableLiveData<T>

fun LiveData<Boolean>.toggle(){
    asMutable.postValue(!value!!)
}

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

class IncrementableIntLiveData(value: Int): LiveData<Int>(value){
    fun increment(){
        postValue(value?.plus(1))
    }
}

class UpdateBlockableLiveData<T>(value: T, private val convertUpdateValue: (T) -> T)
    : LiveData<T>(value){

    fun update(value: T){
        if (blockSubsequentUpdate)
            blockSubsequentUpdate = false
        else
            postValue(convertUpdateValue(value))
    }

    fun blockSubsequentUpdate(){
        blockSubsequentUpdate = true
    }

    private var blockSubsequentUpdate = false
}
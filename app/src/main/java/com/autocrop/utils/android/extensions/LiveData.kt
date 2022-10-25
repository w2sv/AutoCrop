package com.autocrop.utils.android.extensions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

val <T> LiveData<T>.asMutable: MutableLiveData<T>
    get() = this as MutableLiveData<T>

fun LiveData<Boolean>.toggle(){
    asMutable.postValue(!value!!)
}

fun <T> LiveData<T>.postValue(value: T?){
    asMutable.postValue(value)
}
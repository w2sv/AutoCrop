package com.autocrop.utilsandroid

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

val <T> LiveData<T>.mutableLiveData: MutableLiveData<T>
    get() = this as MutableLiveData<T>

fun LiveData<Boolean>.toggleValue(){
    mutableLiveData.postValue(!value!!)
}
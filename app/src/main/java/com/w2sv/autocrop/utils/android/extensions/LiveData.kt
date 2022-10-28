package com.w2sv.autocrop.utils.android.extensions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

fun LiveData<Boolean>.toggle() {
    postValue(!value!!)
}

fun <T> LiveData<T>.postValue(value: T?) {
    asMutable.postValue(value)
}

private val <T> LiveData<T>.asMutable: MutableLiveData<T>
    get() = this as MutableLiveData<T>
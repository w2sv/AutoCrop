package com.autocrop.utils.android

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData

val <T> LiveData<T>.mutableLiveData: MutableLiveData<T>
    get() = this as MutableLiveData<T>
package com.lyrebirdstudio.croppylib.utils.extensions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

val <T> LiveData<T>.asMutable: MutableLiveData<T>
    get() = this as MutableLiveData<T>
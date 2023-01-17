package com.w2sv.autocrop.utils.extensions

import androidx.lifecycle.LiveData
import com.w2sv.androidutils.extensions.postValue

fun LiveData<Int>.increment(){
    postValue(value!! + 1)
}
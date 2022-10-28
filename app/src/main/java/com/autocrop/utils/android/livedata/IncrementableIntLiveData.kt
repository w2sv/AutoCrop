package com.autocrop.utils.android.livedata

import androidx.lifecycle.LiveData

class IncrementableIntLiveData(value: Int) : LiveData<Int>(value) {
    fun increment() {
        postValue(value?.plus(1))
    }
}
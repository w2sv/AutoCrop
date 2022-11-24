package com.w2sv.bidirectionalviewpager.livedata

import androidx.lifecycle.LiveData
import com.w2sv.kotlinutils.delegates.AutoSwitch

class UpdateBlockableLiveData<T>(value: T, private val convertUpdateValue: ((T) -> T)? = null) : LiveData<T>(value) {

    fun update(value: T) {
        if (!blockSubsequentUpdate)
            postValue(
                convertUpdateValue?.invoke(value)
                    ?: value
            )
    }

    fun blockSubsequentUpdate() {
        blockSubsequentUpdate = true
    }

    private var blockSubsequentUpdate by AutoSwitch(value = false, switchOn = true)
}
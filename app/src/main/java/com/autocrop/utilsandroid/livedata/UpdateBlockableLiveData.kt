package com.autocrop.utilsandroid.livedata

import androidx.lifecycle.LiveData
import com.autocrop.utils.delegates.AutoSwitch

class UpdateBlockableLiveData<T>(value: T, private val convertUpdateValue: (T) -> T)
    : LiveData<T>(value){

    fun update(value: T){
        if (!blockSubsequentUpdate)
            postValue(convertUpdateValue(value))
    }

    fun blockSubsequentUpdate(){
        blockSubsequentUpdate = true
    }

    private var blockSubsequentUpdate by AutoSwitch(value = false, switchOn = true)
}
package com.autocrop.utilsandroid.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.junit.Assert
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import utils.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class)
internal class LiveDataKtTest{
    @Test
    fun toggleBooleanLiveData(){
        val liveData: LiveData<Boolean> = MutableLiveData(true)
        liveData.toggle()
        Assert.assertEquals(liveData.value, false)
    }
}
package com.w2sv.autocrop.utilsandroid.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.w2sv.autocrop.utils.extensions.toggle
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import utils.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class)
internal class LiveDataKtTest {
    @Test
    fun toggleBooleanLiveData() {
        val liveData: LiveData<Boolean> = MutableLiveData(true)
        liveData.toggle()
        Assertions.assertEquals(liveData.value, false)
    }
}
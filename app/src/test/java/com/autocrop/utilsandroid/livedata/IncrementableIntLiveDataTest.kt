package com.autocrop.utilsandroid.livedata

import org.junit.Assert
import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import utils.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class)
internal class IncrementableIntLiveDataTest {

    @Test
    fun increment() {
        val liveData = IncrementableIntLiveData(0)
        liveData.increment()
        liveData.increment()

        Assert.assertEquals(2, liveData.value)
    }
}
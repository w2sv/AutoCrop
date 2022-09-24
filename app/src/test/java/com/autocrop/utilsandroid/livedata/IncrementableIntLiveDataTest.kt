package com.autocrop.utilsandroid.livedata

import org.junit.jupiter.api.Assertions.assertEquals
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

        assertEquals(2, liveData.value)
    }
}
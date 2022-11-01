package com.w2sv.bidirectionalviewpager.livedata

import com.w2sv.utils.InstantExecutorExtension
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class)
internal class UpdateBlockableLiveDataTest {

    @Test
    fun update() {
        val liveData = UpdateBlockableLiveData(0) { it }

        liveData.update(1)
        Assertions.assertEquals(1, liveData.value)

        liveData.update(2)
        Assertions.assertEquals(2, liveData.value)

        liveData.blockSubsequentUpdate()
        liveData.update(3)
        Assertions.assertEquals(2, liveData.value)

        liveData.update(7)
        Assertions.assertEquals(7, liveData.value)
    }
}
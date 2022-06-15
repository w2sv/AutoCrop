package com.autocrop.utilsandroid.livedata

import org.junit.Assert
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import utils.InstantExecutorExtension

@ExtendWith(InstantExecutorExtension::class)
internal class UpdateBlockableLiveDataTest {

    @Test
    fun update() {
        val liveData = UpdateBlockableLiveData(0){it}

        liveData.update(1)
        Assert.assertEquals(1, liveData.value)

        liveData.update(2)
        Assert.assertEquals(2, liveData.value)

        liveData.blockSubsequentUpdate()
        liveData.update(3)
        Assert.assertEquals(2, liveData.value)

        liveData.update(7)
        Assert.assertEquals(7, liveData.value)
    }
}
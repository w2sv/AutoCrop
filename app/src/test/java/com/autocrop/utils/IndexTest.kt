package com.autocrop.utils

import org.junit.Assert
import org.junit.Test

internal class IndexTest {
    @Test
    fun rotated() {
        Assert.assertEquals(2, 1.rotated(1, 7))
        Assert.assertEquals(2, 1.rotated(1, 3))
        Assert.assertEquals(0, 1.rotated(-1, 7))
        Assert.assertEquals(6, 1.rotated(-2, 7))
        Assert.assertEquals(5, 1.rotated(4, 7))
        Assert.assertEquals(4, 1.rotated(10, 7))
    }
}
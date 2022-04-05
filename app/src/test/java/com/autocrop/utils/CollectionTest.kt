package com.autocrop.utils

import org.junit.Assert
import org.junit.jupiter.api.Test

internal class CollectionTest {
    @Test
    fun at() = with((0..6).toList()){
        Assert.assertEquals(3, at(3))
        Assert.assertEquals(4, at(-3))
    }

    @Test
    fun rotated() {
        Assert.assertEquals(1, 4.rotated(2, 5))
        Assert.assertEquals(2, 1.rotated(1, 3))
        Assert.assertEquals(0, 1.rotated(1, 2))
        Assert.assertEquals(2, 8.rotated(4, 10))
        Assert.assertEquals(5, 1.rotated(-3, 7))
        Assert.assertEquals(0, 3.rotated(-3, 4))
    }
}
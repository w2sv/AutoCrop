package com.autocrop.utils

import org.junit.Assert
import org.junit.Test

internal class CollectionTest {
    @Test
    fun at() {
        val list: List<Int> = (0..6).toList()

        Assert.assertEquals(3, list.at(3))
        Assert.assertEquals(4, list.at(-3))
    }

    @Test
    fun getByBoolean(){
        val list = listOf(4, 5)

        Assert.assertEquals(4, list[false])
        Assert.assertEquals(5, list[true])
    }
}
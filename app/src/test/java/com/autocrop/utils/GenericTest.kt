package com.autocrop.utils

import org.junit.Assert
import org.junit.Test


internal class GenericTest {
    @Test
    fun booleanToInt(){
        Assert.assertEquals(1, true.toInt())
        Assert.assertEquals(0, false.toInt())
    }
}
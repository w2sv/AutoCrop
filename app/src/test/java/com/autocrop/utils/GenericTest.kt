package com.autocrop.utils

import org.junit.Test
import kotlin.test.assertEquals


internal class GenericTest {
    @Test
    fun booleanToInt(){
        assertEquals(1, true.toInt())
        assertEquals(0, false.toInt())
    }
}
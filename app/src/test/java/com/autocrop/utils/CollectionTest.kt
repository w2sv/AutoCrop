package com.autocrop.utils

import org.junit.Test
import kotlin.test.assertEquals

internal class CollectionTest {
    @Test
    fun at() {
        val list: List<Int> = (0..6).toList()

        assertEquals(3, list.at(3))
        assertEquals(4, list.at(-3))
    }

    @Test
    fun getByBoolean(){
        val list = listOf(4, 5)

        assertEquals(4, list.getByBoolean(false))
        assertEquals(5, list.getByBoolean(true))
    }
}
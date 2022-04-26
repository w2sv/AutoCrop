package com.autocrop.utils

import org.junit.Assert
import org.junit.jupiter.api.Test

internal class RandomTest {
    @Test
    fun randomElement(){
        val arrayList = ArrayList((0..10).toList())

        (0..10).forEach { _ ->
            Assert.assertTrue(Random.randomElement(arrayList) in arrayList)
        }
    }
}
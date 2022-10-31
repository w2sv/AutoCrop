package com.w2sv.autocrop.utils

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class RandomTest {
    @Test
    fun randomElement() {
        val arrayList = ArrayList((0..10).toList())

        (0..10).forEach { _ ->
            Assertions.assertTrue(com.w2sv.autocrop.utils.kotlin.Random.randomElement(arrayList) in arrayList)
        }
    }
}
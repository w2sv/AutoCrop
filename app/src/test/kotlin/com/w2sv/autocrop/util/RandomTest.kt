package com.w2sv.autocrop.util

import com.w2sv.flowfield.Random
import junit.framework.TestCase.assertTrue
import org.junit.Test

internal class RandomTest {
    @Test
    fun randomElement() {
        val arrayList = ArrayList((0..10).toList())

        (0..10).forEach { _ ->
            assertTrue(Random.randomElement(arrayList) in arrayList)
        }
    }
}

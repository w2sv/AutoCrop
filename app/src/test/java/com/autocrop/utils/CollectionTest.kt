package com.autocrop.utils

import org.junit.Assert
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class CollectionTest {
    @ParameterizedTest
    @CsvSource(
        "1, 4, 2, 5",
        "2, 1, 1, 3",
        "0, 1, 1, 2",
        "2, 8, 4, 10",
        "5, 1, -3, 7",
        "0, 3, -3, 4"
    )
    fun rotated(expected: Int, index: Int, distance: Int, collectionSize: Int) {
        Assert.assertEquals(expected, index.rotated(distance, collectionSize))
    }
}
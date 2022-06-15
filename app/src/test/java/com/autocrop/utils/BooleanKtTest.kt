package com.autocrop.utils

import org.junit.Assert
import org.junit.jupiter.api.Assertions.*

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class BooleanKtTest {

    @ParameterizedTest
    @CsvSource(
        "false, 0",
        "true, 1"
    )
    fun toInt(value: Boolean, expected: Int) {
        Assert.assertEquals(value.toInt(), expected)
    }

    @ParameterizedTest
    @CsvSource(
        "false, -1",
        "true, 1"
    )
    fun toNonZeroInt(value: Boolean, expected: Int) {
        Assert.assertEquals(value.toNonZeroInt(), expected)
    }
}
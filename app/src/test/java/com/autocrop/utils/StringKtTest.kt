package com.autocrop.utils

import org.junit.Assert
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class StringKtTest{

    @ParameterizedTest
    @CsvSource(
        "elephant, 0, elephant",
        "elephant, 1, elephant",
        "elephant, 2, elephants",
        "elephant, 652, elephants"
    )
    fun numericallyInflected(string: String, quantity: Int, expected: String){
        Assert.assertEquals(expected, string.numericallyInflected(quantity))
    }

    @ParameterizedTest
    @CsvSource(
        "ayran, Ayran",
        "aYRAN, AYRAN",
        "AYRAN, AYRAN",
    )
    fun capitalized(string: String, expected: String){
        Assert.assertEquals(expected, string.capitalized())
    }
}
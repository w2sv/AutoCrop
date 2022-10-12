package com.autocrop.screencapturelistening.notification

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class ScopeWideUniqueIdsTest {

    companion object{
        private val ids = ScopeWideUniqueIds()
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2, 3, 4, 5])
    fun addNewId(expected: Int) {
        Assertions.assertEquals(expected, ids.addNewId())
    }
}
package com.w2sv.screenshotlistening.notifications

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class UniqueGroupedIdsTest {
    companion object {
        private val ids = UniqueGroupedIds(0)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2, 3, 4, 5])
    fun addNewId(expected: Int) {
        Assertions.assertEquals(expected, ids.getAndAddNewId())
    }
}
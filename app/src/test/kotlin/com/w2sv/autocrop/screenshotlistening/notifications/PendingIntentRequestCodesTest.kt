package com.w2sv.autocrop.screenshotlistening.notifications

import com.w2sv.autocrop.screenshotlistening.PendingIntentRequestCodes
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class PendingIntentRequestCodesTest {

    companion object {
        private val ids = PendingIntentRequestCodes(0)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 2, 3, 4, 5])
    fun addNewId(expected: Int) {
        Assertions.assertEquals(expected, ids.makeAndAdd())
    }
}
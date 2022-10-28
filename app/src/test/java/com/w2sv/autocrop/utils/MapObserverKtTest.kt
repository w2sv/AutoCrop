package com.w2sv.autocrop.utils

import com.w2sv.autocrop.utils.kotlin.delegates.mapObserver
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class MapObserverKtTest {

    companion object {
        var nObserverCalls = 0
    }

    private val map = mutableMapOf("property" to 69)
    private var property by mapObserver(map) { _, _, _ ->
        nObserverCalls += 1
    }

    @ParameterizedTest
    @CsvSource(
        "77, 1",
        "79, 2"
    )
    fun test(newValue: Int, expectedNObserverCalls: Int) {
        property = newValue

        Assertions.assertEquals(expectedNObserverCalls, nObserverCalls)
        Assertions.assertEquals(newValue, map["property"])
        Assertions.assertEquals(newValue, property)
    }
}
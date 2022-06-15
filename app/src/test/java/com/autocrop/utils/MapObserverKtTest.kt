package com.autocrop.utils

import com.autocrop.utils.delegates.mapObserver
import org.junit.Assert
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

internal class MapObserverKtTest{

    companion object{
        var nObserverCalls = 0
    }

    private val map = mutableMapOf("property" to 69)
    private var property by mapObserver(map){ _, _, _ ->
        nObserverCalls += 1
    }

    @ParameterizedTest
    @CsvSource(
        "77, 1",
        "79, 2"
    )
    fun test(newValue: Int, expectedNObserverCalls: Int){
        property = newValue

        Assert.assertEquals(expectedNObserverCalls, nObserverCalls)
        Assert.assertEquals(newValue, map["property"])
        Assert.assertEquals(newValue, property)
    }
}
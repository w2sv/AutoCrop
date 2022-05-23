package com.autocrop.utils

import org.junit.Assert
import org.junit.jupiter.api.Test

internal class MapDelegateObserverKtTest{
    private val map = mutableMapOf("property" to 69)
    private var nObserverCalled = 0

    private var property by mapDelegateObserver(map){ _, _, _ ->
        nObserverCalled += 1
    }

    @Test
    fun functionality(){
        val newValue = 77

        property = newValue

        Assert.assertEquals(1, nObserverCalled)
        Assert.assertEquals(newValue, map["property"])
        Assert.assertEquals(newValue, property)
    }
}
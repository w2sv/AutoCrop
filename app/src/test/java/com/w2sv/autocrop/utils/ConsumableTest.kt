package com.w2sv.autocrop.utils

import com.w2sv.autocrop.utils.kotlin.delegates.Consumable
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class ConsumableTest {

    companion object {
        const val INITIAL_VALUE = 69
    }

    private var consumable by Consumable<Int>()

    @Test
    fun settingAndValueConsumptionOnGetValue() {
        Assertions.assertNull(consumable)

        consumable = INITIAL_VALUE
        Assertions.assertEquals(INITIAL_VALUE, consumable)
        Assertions.assertNull(consumable)
    }
}
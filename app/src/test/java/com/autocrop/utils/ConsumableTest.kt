package com.autocrop.utils

import com.autocrop.utils.delegates.Consumable
import org.junit.Assert
import org.junit.jupiter.api.Test

internal class ConsumableTest{

    companion object{
        const val INITIAL_VALUE = 69
    }

    private var consumable by Consumable<Int>()

    @Test
    fun settingAndValueConsumptionOnGetValue(){
        Assert.assertNull(consumable)

        consumable = INITIAL_VALUE
        Assert.assertEquals(INITIAL_VALUE, consumable)
        Assert.assertNull(consumable)
    }
}
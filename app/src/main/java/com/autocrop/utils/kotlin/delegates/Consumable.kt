package com.autocrop.utils.kotlin.delegates

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class Consumable<T>(private var value: T? = null) : ReadWriteProperty<Any, T?> {

    override fun getValue(thisRef: Any, property: KProperty<*>): T? =
        value
            .also { value = null }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
        this.value = value
    }
}
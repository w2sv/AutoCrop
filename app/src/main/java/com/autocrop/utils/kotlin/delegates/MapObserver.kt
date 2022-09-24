package com.autocrop.utils.kotlin.delegates

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * @see:
 *     https://stackoverflow.com/a/52814429/12083276
 */
inline fun <T> mapObserver(
    map: MutableMap<String, T>,
    crossinline observe: ((property: KProperty<*>, oldValue: T, newValue: T) -> Unit)
)
        : ReadWriteProperty<Any?, T> {

    return object : ReadWriteProperty<Any?, T> {

        override fun getValue(thisRef: Any?, property: KProperty<*>): T =
            map.getValue(property.name)

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            val oldValue = getValue(thisRef, property)
            map[property.name] = value
            observe(property, oldValue, value)
        }
    }
}
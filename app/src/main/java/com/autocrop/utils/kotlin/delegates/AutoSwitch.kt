package com.autocrop.utils.kotlin.delegates

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class AutoSwitch(private var value: Boolean, private val switchOn: Boolean) : ReadWriteProperty<Any?, Boolean> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean =
        value
            .also {
                if (it == switchOn)
                    value = !it
            }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
        this.value = value
    }

    class Mapped(
        private val map: MutableMap<String, Boolean>,
        private val switchOn: Boolean
    ) : ReadWriteProperty<Any?, Boolean> {

        override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean =
            map.getValue(property.name)
                .also {
                    if (it == switchOn)
                        map[property.name] = !it
                }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
            map[property.name] = value
        }
    }
}


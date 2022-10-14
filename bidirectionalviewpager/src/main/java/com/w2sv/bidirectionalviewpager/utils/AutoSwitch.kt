package com.w2sv.bidirectionalviewpager.utils

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class AutoSwitch(private var value: Boolean, private val switchOn: Boolean)
    : ReadWriteProperty<Any?, Boolean> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): Boolean =
        value
            .also {
                if (it == switchOn)
                    value = !it
            }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
        this.value = value
    }
}


package com.autocrop.utils


fun Boolean.toInt(): Int = this.compareTo(false)

fun String.replaceMultiple(vararg oldValue: String, newValue: String): String = this.run{
    var copy: String = this

    oldValue.forEach {
        copy = copy.replace(it, newValue)
    }
    copy
}
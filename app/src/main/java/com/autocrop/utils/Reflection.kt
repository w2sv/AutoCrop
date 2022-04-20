package com.autocrop.utils

inline fun <reified T> Any.reflectField(name: String): T =
    javaClass.getField(name).get(this) as T

inline fun <reified T> Any.reflectMethod(name: String): T =
    javaClass.getMethod("get${name.capitalized()}").invoke(this) as T
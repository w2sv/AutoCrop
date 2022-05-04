package com.autocrop.utils

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

fun Any.typeArgument(): Type =
    (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
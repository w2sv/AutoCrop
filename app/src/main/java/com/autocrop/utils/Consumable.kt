package com.autocrop.utils

class Consumable<T>(private var value: T? = null){

    fun consume(): T? = value
        .also { value = null }

    fun set(value: T){
        this.value = value
    }
}
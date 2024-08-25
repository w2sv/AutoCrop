package com.w2sv.autocrop.ui.util

import androidx.lifecycle.LiveData

val <T> LiveData<T>.nonNullValue: T
    get() = requireNotNull(value)
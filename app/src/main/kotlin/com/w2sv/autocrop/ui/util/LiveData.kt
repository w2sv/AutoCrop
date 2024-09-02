package com.w2sv.autocrop.ui.util

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

/**
 * ======== TAKEN FROM RADX ========
 *
 * Subscribe the given fragment by its view lifecycle to a LiveData property in a null-safe way. The
 * given observer will not be called when the LiveData's value is null.
 */
fun <T> LiveData<T>.subscribe(fragment: Fragment, observer: (T) -> Unit) {
    observe(fragment.viewLifecycleOwner) {
        if (it != null) {
            observer(it)
        }
    }
}

/**
 * Updates the current value as per [update] and posts it.
 */
inline fun <T> MutableLiveData<T>.postUpdatedValue(update: (T) -> T) {
    value?.let {
        postValue(update(it))
    }
}

/**
 * Updates the current value as per [update] and sets it.
 */
inline fun <T> MutableLiveData<T>.setUpdatedValue(update: (T) -> T) {
    value?.let {
        value = update(it)
    }
}

/**
 * Throws an [IllegalArgumentException] if value is null. Otherwise returns the non null value.
 */
val <T> LiveData<T>.nonNullValue: T
    get() = requireNotNull(value)
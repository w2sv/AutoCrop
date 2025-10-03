package com.w2sv.autocrop.ui.util

import com.w2sv.datastoreutils.datastoreflow.DataStoreFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

fun <O, R> DataStoreFlow<O>.transformedMutableStateIn( // TODO: move to DataStoreUtils
    scope: CoroutineScope,
    transform: (O) -> R
): MutableStateFlow<R> {
    val initialValue = default()
    val mutableFlow = MutableStateFlow(transform(initialValue))

    scope.launch {
        val firstValue = first()
        if (firstValue != initialValue) {
            mutableFlow.value = transform(firstValue)
        }
    }

    return mutableFlow
}

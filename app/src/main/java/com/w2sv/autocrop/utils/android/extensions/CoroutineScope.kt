package com.w2sv.autocrop.utils.android.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun <P, R> CoroutineScope.executeAsyncTaskWithProgressUpdateReceiver(
    task: suspend (suspend (P) -> Unit) -> R,
    onProgressUpdate: (P) -> Unit,
    onFinished: (R) -> Unit
): Job =
    launch {
        onFinished(
            withContext(Dispatchers.IO) {
                task {
                    withContext(Dispatchers.Main) {
                        onProgressUpdate(it)
                    }
                }
            }
        )
    }

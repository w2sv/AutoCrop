package com.autocrop.utils.kotlin.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun <R> CoroutineScope.executeAsyncTask(
    doInBackground: () -> R,
    onPostExecute: ((R) -> Unit)? = null
) = launch {
        val result = withContext(Dispatchers.IO) {
            doInBackground ()
        }
        onPostExecute?.run { invoke(result) }
    }

fun <P, R> CoroutineScope.executeAsyncTask(
    doInBackground: suspend (suspend (P) -> Unit) -> R,
    onProgressUpdate: (P) -> Unit,
    onPostExecute: (R) -> Unit
) = launch {
        val result = withContext(Dispatchers.IO) {
            doInBackground {
                withContext(Dispatchers.Main) { onProgressUpdate(it) }
            }
        }
        onPostExecute(result)
}
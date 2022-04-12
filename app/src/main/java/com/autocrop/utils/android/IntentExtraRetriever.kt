package com.autocrop.utils.android

import android.content.Intent

/**
 * Takes care of retrieving put extra only once after instantiation
 */
class IntentExtraRetriever<T>(private val extraName: String) {
    private var intentConsumed: Boolean = false

    operator fun invoke(intent: Intent, defaultValue: T? = null): T?{
        if (!intentConsumed){
            intent.extras?.let { bundle ->
                bundle.get(extraName)?.let { value ->
                    @Suppress("UNCHECKED_CAST")
                    if (defaultValue == null || value != defaultValue)
                        return (value as T).also { intentConsumed = true }
                }
            }
        }
        return null
    }
}
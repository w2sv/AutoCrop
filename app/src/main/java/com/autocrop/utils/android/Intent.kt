package com.autocrop.utils.android

import android.content.Intent


/**
 * Takes care of retrieving put extra only once after instantiation
 */
class IntentExtraRetriever {
    private var intentConsumed: Boolean = false

    operator fun invoke(intent: Intent, extraName: String, defaultValue: Int): Int?{
        if (!intentConsumed){
            intent.getIntExtra(extraName, defaultValue).let {
                if (it != defaultValue)
                    return it.also { intentConsumed = true }
            }
        }
        return null
    }
}
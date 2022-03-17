package com.autocrop

import android.content.SharedPreferences
import com.autocrop.utils.android.writeBoolean
import timber.log.Timber
import java.util.*


/**
 * Singleton encapsulating entirety of parameters set by user
 * having a global impact
 */
object UserPreferences : SortedMap<String, Boolean> by sortedMapOf(
    Keys.conductAutoScrolling to true,
    Keys.deleteInputScreenshots to true) {

    object Keys {
        const val conductAutoScrolling: String = "CONDUCT_AUTO_SCROLL"
        const val deleteInputScreenshots: String = "DELETE_INPUT_SCREENSHOTS"
    }

    val conductAutoScroll: Boolean
        get() = get(Keys.conductAutoScrolling)!!
    val deleteInputScreenshots: Boolean
        get() = get(Keys.deleteInputScreenshots)!!

    var isInitialized: Boolean = false

    fun init(defaultSharedPreferences: SharedPreferences) {
        keys.forEach {
            this[it] = defaultSharedPreferences.getBoolean(
                it,
                get(it)!!
            )
        }
        Timber.i("Initialized Parameters")

        isInitialized = true
    }

    fun toggle(key: String) {
        this[key] = !this[key]!!
        Timber.i("Toggled $key to ${this[key]}")
    }

    fun writeToSharedPreferences(
        previousValues: List<Boolean>,
        defaultSharedPreferences: SharedPreferences
    ) {
        (keys zip (previousValues zip values)).forEach {
            with(it.second) {
                if (first != second)
                    defaultSharedPreferences.writeBoolean(
                        it.first,
                        second
                    )
                Timber.i("Set SharedPreferences.${it.first} to $second")
            }
        }
    }
}
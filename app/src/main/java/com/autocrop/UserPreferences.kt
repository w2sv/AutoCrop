package com.autocrop

import android.content.SharedPreferences
import com.autocrop.utils.android.writeBoolean
import timber.log.Timber
import java.util.*


/**
 * Singleton encapsulating entirety of parameters set by user
 * having a global impact
 */
object UserPreferences : SortedMap<String, Boolean> by sortedMapOf() {

    val sharedPreferencesFileName: String = this.javaClass.name

    object Keys {
        const val conductAutoScrolling: String = "CONDUCT_AUTO_SCROLL"
        const val deleteInputScreenshots: String = "DELETE_INPUT_SCREENSHOTS"
    }

    val isInitialized: Boolean
        get() = isNotEmpty()

    fun init(sharedPreferences: SharedPreferences) {
        val defaultValue: Map<String, Boolean> = mapOf(
            Keys.conductAutoScrolling to true,
            Keys.deleteInputScreenshots to false
        )

        keys.forEach {
            this[it] = sharedPreferences.getBoolean(it, defaultValue[it]!!)
        }
        Timber.i("Initialized Parameters")
    }

    /**
     * Expose values as variables for convenience
     */
    val conductAutoScroll: Boolean
        get() = getValue(Keys.conductAutoScrolling)
    val deleteInputScreenshots: Boolean
        get() = getValue(Keys.deleteInputScreenshots)

    fun toggle(key: String) {
        this[key] = !this[key]!!
        Timber.i("Toggled $key to ${this[key]}")
    }

    fun writeToSharedPreferences(sharedPreferences: SharedPreferences) {
        (keys zip values).forEach { (key, value) ->
            sharedPreferences.writeBoolean(key, value)
            Timber.i("Set SharedPreferences.$key to $value")
        }
    }
}
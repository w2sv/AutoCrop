package com.autocrop

import android.content.SharedPreferences
import com.autocrop.utils.android.writeBoolean
import com.autocrop.utils.logAfterwards
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

    fun init(sharedPreferences: SharedPreferences) = logAfterwards("Initialized UserPreferences") {
        mapOf(
            Keys.conductAutoScrolling to true,
            Keys.deleteInputScreenshots to false
        ).forEach{ (key, defaultValue) ->
            this[key] = sharedPreferences.getBoolean(key, defaultValue)
        }
    }

    /**
     * Expose values as variables for convenience
     */
    val conductAutoScroll: Boolean
        get() = getValue(Keys.conductAutoScrolling)
    val deleteInputScreenshots: Boolean
        get() = getValue(Keys.deleteInputScreenshots)

    fun toggle(key: String) = logAfterwards("Toggled $key to ${this[key]}"){
        this[key] = !this[key]!!
    }

    fun writeToSharedPreferences(sharedPreferences: SharedPreferences) = logAfterwards("Wrote UserPreferences to sharedPreferences") {
        (keys zip values).forEach { (key, value) ->
            sharedPreferences.writeBoolean(key, value)
        }
    }
}
package com.autocrop

import android.content.SharedPreferences
import com.autocrop.utils.android.writeBoolean
import com.autocrop.utils.logAfterwards
import timber.log.Timber
import java.util.*


/**
 * Singleton encapsulating entirety of parameters set by user
 * having a global impact
 */
object UserPreferences : SortedMap<String, Boolean> by sortedMapOf() {
    object Keys {
        const val conductAutoScrolling: String = "CONDUCT_AUTO_SCROLL"
        const val deleteIndividualScreenshot = "DELETE_INDIVIDUAL_SCREENSHOT"
        const val deleteScreenshotsOnSaveAll: String = "DELETE_SCREENSHOTS_ON_SAVE_ALL"
    }

    val isInitialized: Boolean
        get() = isNotEmpty()

    fun initializeFromSharedPreferences(sharedPreferences: SharedPreferences) = logAfterwards("Initialized ${javaClass.name}") {
        mapOf(
            Keys.conductAutoScrolling to true,
            Keys.deleteIndividualScreenshot to false,
            Keys.deleteScreenshotsOnSaveAll to false
        ).forEach{ (key, defaultValue) ->
            this[key] = sharedPreferences.getBoolean(key, defaultValue)
        }
    }

    /**
     * Expose values as variables for convenience
     */
    val conductAutoScrolling: Boolean
        get() = getValue(Keys.conductAutoScrolling)
    val deleteIndividualScreenshot: Boolean
        get() = getValue(Keys.deleteIndividualScreenshot)
    val deleteScreenshotsOnSaveAll: Boolean
        get() = getValue(Keys.deleteScreenshotsOnSaveAll)

    /**
     * Toggles respective value & respective value within [hasChangedSinceLastWriteToSharedPreferences]
     * and logs
     */
    fun toggle(key: String) = logAfterwards("Toggled $key to ${this[key]}"){
        this[key] = !getValue(key)
        hasChangedSinceLastWriteToSharedPreferences[key] = !hasChangedSinceLastWriteToSharedPreferences.getValue(key)
    }

    /**
     * Keep track of which values have changed since last writing operation to shared preferences
     * to reduce number of conducted IO operations
     */
    private val hasChangedSinceLastWriteToSharedPreferences: MutableMap<String, Boolean> = keys.zip(List(size){false}).toMap().toMutableMap()

    fun writeChangedValuesToSharedPreferences(sharedPreferences: Lazy<SharedPreferences>) =
        keys
            .filter { hasChangedSinceLastWriteToSharedPreferences.getValue(it) }
            .forEach {
                sharedPreferences.value.writeBoolean(it, getValue(it))
                Timber.i("Wrote $it=${getValue(it)} to shared preferences")
            }
}
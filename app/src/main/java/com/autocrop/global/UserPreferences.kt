package com.autocrop.global

import android.content.SharedPreferences
import android.net.Uri
import com.autocrop.utils.android.writeBoolean
import com.autocrop.utils.logAfterwards
import timber.log.Timber
import java.util.*

abstract class UserPreferences<T>(private val defaultValue: Map<String, T>): SortedMap<String, T> by sortedMapOf(){
    fun initializeFromSharedPreferences(sharedPreferences: SharedPreferences) = logAfterwards("Initialized ${javaClass.name}") {
        defaultValue.forEach{ (key, defaultValue) ->
            this[key] = sharedPreferences.getValue(key, defaultValue)
            hasChangedSinceLastWriteToSharedPreferences[key] = false
        }
    }

    /**
     * Keep track of which values have changed since last writing operation to shared preferences
     * to reduce number of conducted IO operations
     */
    protected val hasChangedSinceLastWriteToSharedPreferences: MutableMap<String, Boolean> = mutableMapOf()

    val isInitialized: Boolean
        get() = isNotEmpty()

    fun writeChangedValuesToSharedPreferences(sharedPreferences: Lazy<SharedPreferences>) =
        keys
            .filter { hasChangedSinceLastWriteToSharedPreferences.getValue(it) }
            .forEach {
                sharedPreferences.value.writeValue(it, getValue(it))
                Timber.i("Wrote $it=${getValue(it)} to shared preferences")
            }

    protected abstract fun SharedPreferences.writeValue(key: String, value: T)
    protected abstract fun SharedPreferences.getValue(key: String, defaultValue: T): T
}

/**
 * Singleton encapsulating entirety of parameters set by user
 * having a global impact
 */
object BooleanUserPreferences : UserPreferences<Boolean>(
    mapOf(
        Keys.conductAutoScrolling to true,
        Keys.deleteIndividualScreenshot to false,
        Keys.deleteScreenshotsOnSaveAll to false
    )
) {

    object Keys {
        const val conductAutoScrolling: String = "CONDUCT_AUTO_SCROLL"
        const val deleteIndividualScreenshot = "DELETE_INDIVIDUAL_SCREENSHOT"
        const val deleteScreenshotsOnSaveAll: String = "DELETE_SCREENSHOTS_ON_SAVE_ALL"
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
        this[key] = !(getValue(key) as Boolean)
        hasChangedSinceLastWriteToSharedPreferences[key] = !hasChangedSinceLastWriteToSharedPreferences.getValue(key)
    }

    override fun SharedPreferences.writeValue(key: String, value: Boolean) = writeBoolean(key, value)
    override fun SharedPreferences.getValue(key: String, defaultValue: Boolean): Boolean = getBoolean(key, defaultValue)
}

object UriUserPreferences: UserPreferences<Uri?>(
    mapOf(
        Keys.imageSaveDestination to null,
        Keys.treeUri to null
    )
){

    object Keys{
        const val treeUri: String = "TREE_URI"
        const val imageSaveDestination: String = "IMAGE_SAVE_DESTINATION"
    }

    var imageSaveDestination: Uri?
        get() = getValue(Keys.imageSaveDestination)
        set(value){
            this[Keys.imageSaveDestination] = value
        }

    var treeUri: Uri?
        get() = getValue(Keys.treeUri)
        set(value){
            this[Keys.treeUri] = value
        }

    override fun SharedPreferences.writeValue(key: String, value: Uri?) = edit().putString(key, value.toString()).apply()
    override fun SharedPreferences.getValue(key: String, defaultValue: Uri?): Uri? = Uri.parse(getString(key, defaultValue.toString()))
}
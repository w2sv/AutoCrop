package com.autocrop.global

import android.content.SharedPreferences
import android.net.Uri
import com.autocrop.utils.android.buildDocumentUriFromTreeUri
import com.autocrop.utils.logBeforehand
import timber.log.Timber
import java.util.*

abstract class UserPreferences<T>(default: SortedMap<String, T>)
    : SortedMap<String, T> by default{

    /**
     * Also copy set values to [lastDiscSyncState] before exiting function
     */
    fun initializeFromSharedPreferences(sharedPreferences: SharedPreferences) = logBeforehand("Initializing ${javaClass.name} from SharedPreferences") {
        forEach{ (key, defaultValue) ->
            put(key, sharedPreferences.getValue(key, defaultValue))
            Timber.i("Set ${javaClass.name}.$key to $defaultValue from SharedPreferences")
        }
        lastDiscSyncState = toMutableMap()
    }

    protected abstract fun SharedPreferences.writeValue(key: String, value: T)
    protected abstract fun SharedPreferences.getValue(key: String, defaultValue: T): T

    fun writeChangedValuesToSharedPreferences(sharedPreferences: Lazy<SharedPreferences>) =
        entries
            .filter { lastDiscSyncState.getValue(it.key) != it.value }
            .forEach {
                sharedPreferences.value.writeValue(it.key, it.value)
                lastDiscSyncState[it.key] = it.value
                Timber.i("Wrote ${it.key}=${it.value} to shared preferences")
            }

    /**
     * Keep track of which values have changed since last call to [writeChangedValuesToSharedPreferences]
     * to reduce number of IO operations
     */
    private lateinit var lastDiscSyncState: MutableMap<String, T>
}

/**
 * Singleton encapsulating entirety of parameters set by user
 * having a global impact
 */
object BooleanUserPreferences : UserPreferences<Boolean>(
    sortedMapOf(
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

    fun toggle(key: String){
        put(key, !getValue(key))
    }

    override fun SharedPreferences.writeValue(key: String, value: Boolean) = edit().putBoolean(key, value).apply()
    override fun SharedPreferences.getValue(key: String, defaultValue: Boolean): Boolean = getBoolean(key, defaultValue)
}

object CropFileSaveDestinationPreferences: UserPreferences<Uri?>(sortedMapOf(Keys.treeUri to null)) {

    object Keys {
        const val treeUri: String = "IMAGE_SAVE_DESTINATION_TREE_URI"
    }

    /**
     * Inherently build [documentUri] upon setting new [treeUri]
     */
    override fun put(key: String?, value: Uri?): Uri? {
        println("value: $value")
        println("value: ${value == null}")

        if (key == Keys.treeUri && value != getOrDefault(Keys.treeUri, null))
            documentUri = buildDocumentUriFromTreeUri(value!!)
                .also { Timber.i("Set documentUri $it") }

        return super.put(key, value)
    }

    var treeUri: Uri?
        get() = getValue(Keys.treeUri)
        set(value) {
            put(Keys.treeUri, value)
        }

    var documentUri: Uri? = null

    override fun SharedPreferences.writeValue(key: String, value: Uri?) =
        edit().putString(key, value?.run { toString() }).apply()
    override fun SharedPreferences.getValue(key: String, defaultValue: Uri?): Uri? =
        getString(key, defaultValue.run { this?.toString() })?.run {
            Uri.parse(this)
        }
}
val userPreferencesInstances = arrayOf(BooleanUserPreferences, CropFileSaveDestinationPreferences)
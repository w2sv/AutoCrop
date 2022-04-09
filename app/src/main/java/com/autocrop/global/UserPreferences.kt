package com.autocrop.global

import android.content.SharedPreferences
import android.net.Uri
import android.provider.DocumentsContract
import com.autocrop.utils.logAfterwards
import com.autocrop.utils.logBeforehand
import timber.log.Timber
import java.util.*

abstract class UserPreferences<T>(default: SortedMap<String, T>): SortedMap<String, T> by default{
    fun initializeFromSharedPreferences(sharedPreferences: SharedPreferences) = logBeforehand("Initializing ${javaClass.name} from SharedPreferences") {
        forEach{ (key, defaultValue) ->
            this[key] = sharedPreferences.getValue(key, defaultValue)
            Timber.i("Set ${javaClass.name}.$key to $defaultValue from SharedPreferences")
        }
    }

    /**
     * Keep track of which values have changed since last writing operation to shared preferences
     * to reduce number of conducted IO operations
     */
    protected val changedSinceLastDiscSync: MutableMap<String, Boolean> =
        mutableMapOf<String, Boolean>().withDefault { false }

    fun writeChangedValuesToSharedPreferences(sharedPreferences: Lazy<SharedPreferences>) =
        keys
            .filter { changedSinceLastDiscSync.getValue(it) }
            .forEach {
                sharedPreferences.value.writeValue(it, getValue(it))
                changedSinceLastDiscSync[it] = false
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

    /**
     * Toggles respective value & respective value within [changedSinceLastDiscSync]
     * and logs
     */
    fun toggle(key: String) = logAfterwards("Toggled $key to ${this[key]}"){
        this[key] = !(getValue(key) as Boolean)
        changedSinceLastDiscSync[key] = !changedSinceLastDiscSync.getValue(key)
    }

    override fun SharedPreferences.writeValue(key: String, value: Boolean) = edit().putBoolean(key, value).apply()
    override fun SharedPreferences.getValue(key: String, defaultValue: Boolean): Boolean = getBoolean(key, defaultValue)
}

object SaveDestinationPreferences: UserPreferences<Uri?>(sortedMapOf(Keys.treeUri to null)){

    object Keys{
        const val treeUri: String = "IMAGE_SAVE_DESTINATION_TREE_URI"
    }

    var treeUri: Uri?
        get() = getValue(Keys.treeUri)
        set(value){
//            if (value != getValue(Keys.treeUri)){
                this[Keys.treeUri] = value
                setDocumentUri(value!!)
                changedSinceLastDiscSync[Keys.treeUri] = true
//            }
        }

    val documentUri: Uri?
        get() = when{
            _documentUri == null && treeUri != null -> {
                setDocumentUri(treeUri!!)
                _documentUri
            }
            else -> _documentUri
        }
    private var _documentUri: Uri? = null
    private fun setDocumentUri(treeUri: Uri){
        _documentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, DocumentsContract.getTreeDocumentId(treeUri))
        Timber.i("Set documentUri $_documentUri")
    }

    override fun SharedPreferences.writeValue(key: String, value: Uri?) = edit().putString(key, value.toString()).apply()
    override fun SharedPreferences.getValue(key: String, defaultValue: Uri?): Uri? = Uri.parse(getString(key, defaultValue.toString()))
}
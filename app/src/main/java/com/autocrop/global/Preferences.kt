package com.autocrop.global

import android.content.SharedPreferences
import android.net.Uri
import com.autocrop.utils.logBeforehand
import com.autocrop.utils.mapDelegateObserver
import com.autocrop.utilsandroid.buildDocumentUriFromTreeUri
import timber.log.Timber

abstract class Preferences<T>(protected val map: MutableMap<String, T>)
    : MutableMap<String, T> by map{

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
object BooleanPreferences : Preferences<Boolean>(
    mutableMapOf(
        "autoScroll" to true,
        "deleteScreenshots" to false,
        "viewPagerInstructionsShown" to false,
        "comparisonInstructionsShown" to false,
        "aboutFragmentInstructionsShown" to false
    )
) {
    /**
     * Expose values as delegated variables for convenience
     */
    var autoScroll by map
    var deleteScreenshots by map
    var viewPagerInstructionsShown by map
    var comparisonInstructionsShown by map
    var aboutFragmentInstructionsShown by map

    override fun SharedPreferences.writeValue(key: String, value: Boolean) = edit().putBoolean(key, value).apply()
    override fun SharedPreferences.getValue(key: String, defaultValue: Boolean): Boolean = getBoolean(key, defaultValue)
}

object CropSavingPreferences: Preferences<Uri?>(
    mutableMapOf("treeUri" to null)
) {
    
    /**
     * Inherently build [documentUri] upon setting new [treeUri]
     */
    var treeUri: Uri? by mapDelegateObserver(map){ _, oldValue, newValue ->
        if (newValue != null && oldValue != newValue)
            _documentUri = buildDocumentUriFromTreeUri(newValue)
                .also { Timber.i("Set new documentUri: $it") }
    }

    val documentUri: Uri?
        get() = _documentUri
    private var _documentUri: Uri? = null

    override fun SharedPreferences.writeValue(key: String, value: Uri?) =
        edit().putString(key, value?.run { toString() }).apply()
    override fun SharedPreferences.getValue(key: String, defaultValue: Uri?): Uri? =
        getString(key, defaultValue.run { this?.toString() })?.run {
            Uri.parse(this)
        }
}

val preferencesInstances = arrayOf(BooleanPreferences, CropSavingPreferences)
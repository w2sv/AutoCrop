package com.autocrop.preferences

import android.content.SharedPreferences
import de.paul_woitaschek.slimber.i

/**
 * Base for KEY to VALUE map delegator objects, the content of which is to be stored in [SharedPreferences]
 */
sealed class TypedPreferences<T>(protected val map: MutableMap<String, T>)
    : MutableMap<String, T> by map{

    /**
     * init{} substitute, hence to be called before whatever sort of object usage
     *
     * Initializes values with the ones contained in [sharedPreferences] instance
     * and copies them to [lastDiscSyncState]
     */
    fun initializeFromSharedPreferences(sharedPreferences: SharedPreferences){
        i{"Initializing ${javaClass.name} from SharedPreferences"}

        forEach{ (key, defaultValue) ->
            put(key, sharedPreferences.getValue(key, defaultValue))
            i{"Set ${javaClass.name}.$key to $defaultValue from SharedPreferences"}
        }
        lastDiscSyncState = toMutableMap()
    }

    fun writeChangedValuesToSharedPreferences(sharedPreferences: Lazy<SharedPreferences>) =
        entries
            .filter { lastDiscSyncState.getValue(it.key) != it.value }
            .forEach {
                sharedPreferences.value.writeValue(it.key, it.value)
                i{"Wrote ${it.key}=${it.value} to shared preferences"}

                lastDiscSyncState[it.key] = it.value
            }

    /**
     * Keep track of which values have changed since last call to [writeChangedValuesToSharedPreferences]
     * to reduce number of IO operations
     */
    private lateinit var lastDiscSyncState: MutableMap<String, T>

    /**
     * Type-specific value fetching from and writing to [SharedPreferences]
     */
    protected abstract fun SharedPreferences.writeValue(key: String, value: T)
    protected abstract fun SharedPreferences.getValue(key: String, defaultValue: T): T
}
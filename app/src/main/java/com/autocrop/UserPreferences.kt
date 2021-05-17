package com.autocrop

import android.content.SharedPreferences
import android.os.Build
import android.os.Environment
import com.autocrop.utils.android.writeBoolean
import timber.log.Timber
import java.io.File
import java.util.*


/**
 * Singleton encapsulating entirety of parameters set by user
 * having a global impact
 */
object UserPreferences : SortedMap<String, Boolean> by sortedMapOf(
    Keys.conductAutoScrolling to true,
    Keys.deleteInputScreenshots to true,
    Keys.saveToAutocroppedDir to true) {

    object Keys {
        const val conductAutoScrolling: String = "CONDUCT_AUTO_SCROLL"
        const val deleteInputScreenshots: String = "DELETE_INPUT_SCREENSHOTS"
        const val saveToAutocroppedDir: String = "SAVE_TO_AUTOCROPPED_DIR"
    }

    val conductAutoScroll: Boolean
        get() = get(Keys.conductAutoScrolling)!!
    val deleteInputScreenshots: Boolean
        get() = get(Keys.deleteInputScreenshots)!!
    val saveToAutocroppedDir: Boolean
        get() = get(Keys.saveToAutocroppedDir)!!

    fun init(defaultSharedPreferences: SharedPreferences) {
        keys.forEach {
            this[it] = defaultSharedPreferences.getBoolean(
                it,
                get(it)!!
            )
        }
            .also {
                Timber.i("Initialized Parameters")
            }

        isInitialized = true
    }

    var isInitialized: Boolean = false

    fun toggle(key: String) {
        this[key] = !this[key]!!
        Timber.i("Toggled $key to ${this[key]}")

        when (key) {
            Keys.saveToAutocroppedDir -> saveToAutocropDirTogglingEcho()
            else -> Unit
        }
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

    val relativeCropSaveDirPath: String
        get() = Environment.DIRECTORY_PICTURES.run {
            if (!saveToAutocroppedDir)
                this
            else
                File(this, "AutoCropped").path
        }

    val absoluteCropSaveDirPath: String
        get() = Environment.getExternalStoragePublicDirectory(relativeCropSaveDirPath).absolutePath

    /**
     * Creates AutoCrop dir in external storage pictures directory if saveToAutocropDir
     * true after toggling, dir not yet existent and Version < Q, beginning from which
     * directories are created automatically
     */
    private fun saveToAutocropDirTogglingEcho() {
        if (saveToAutocroppedDir)
            makeAutoCroppedDirIfRequired()
    }

    fun makeAutoCroppedDirIfRequired(): Boolean {
        with(File(absoluteCropSaveDirPath)) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !exists())
                return mkdirs().also {
                    if (it)
                        Timber.i("Created AutoCropped directory under $absolutePath")
                    else
                        Timber.i("Couldn't create AutoCropped directory")
                }
        }
        return false
    }
}
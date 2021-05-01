package com.autocrop

import android.content.SharedPreferences
import android.os.Build
import android.os.Environment
import com.autocrop.utils.android.writeBoolean
import com.autocrop.utils.toInt
import timber.log.Timber
import java.io.File


enum class PreferenceParameter {
    DeleteInputScreenshots,
    SaveToAutocropDir;

    companion object{
        operator fun get(ordinal: Int): PreferenceParameter = values()[ordinal]
        val size: Int
            get() = values().size
    }
}


/**
 * Singleton encapsulating entirety of parameters set by user
 * having a global impact
 */
object UserPreferences {
    private lateinit var values: Array<Boolean>

    val isInitialized: Boolean
        get() = this::values.isInitialized

    fun clone(): Array<Boolean> = values.clone()
    operator fun get(parameter: PreferenceParameter): Boolean = values[parameter.ordinal]
    private operator fun set(parameter: PreferenceParameter, value: Boolean) {
        values[parameter.ordinal] = value.also {
            Timber.i("Set GlobalParameters.${parameter.name} to $it")
        }
    }

    fun toggle(parameter: PreferenceParameter) {
        this[parameter] = !this[parameter]
        Timber.i("Toggled ${parameter.name} to ${this[parameter]}")

        when (parameter) {
            PreferenceParameter.SaveToAutocropDir -> saveToAutocropDirTogglingEcho()
            else -> Unit
        }
    }

    fun initialize(defaultSharedPreferences: SharedPreferences) {
        val parameterToDefaultValue: Map<PreferenceParameter, Boolean> = mapOf(
            PreferenceParameter.DeleteInputScreenshots to false,
            PreferenceParameter.SaveToAutocropDir to true
        )

        values = Array(PreferenceParameter.size) {
            PreferenceParameter[it].run {
                defaultSharedPreferences.getBoolean(
                    this.name,
                    parameterToDefaultValue[this]!!
                )
            }
        }.also {
            Timber.i("Initialized Parameters")
        }
    }

    fun writeToSharedPreferences(
        previousValues: Array<Boolean>,
        defaultSharedPreferences: SharedPreferences
    ) {
        (values zip previousValues).forEachIndexed { index, valuePair ->
            with(valuePair) {
                if (first != second)
                    defaultSharedPreferences.writeBoolean(
                        PreferenceParameter[index].name,
                        first
                    ).also {
                        Timber.i("Set SharedPreferences.${PreferenceParameter[index].name} to $first")
                    }
            }
        }
    }

    // ---------------deleteInputScreenshots-------------

    val deleteInputScreenshots: Boolean
        get() = this[PreferenceParameter.DeleteInputScreenshots]

    // -------------saveToAutocropDir--------------------

    val saveToAutocropDir: Boolean
        get() = this[PreferenceParameter.SaveToAutocropDir]

    /**
     * Equals {Environment.DIRECTORY_PICTURES} or {Environment.DIRECTORY_PICTURES + autocrop_dirname}
     * if saveToAutocropDir set to true
     */
    val relativeCropSaveDirPath: String
        get() = "${Environment.DIRECTORY_PICTURES}${
            listOf(
                "",
                "${File.separator}AutoCropped"
            )[saveToAutocropDir.toInt()]
        }"

    private var autoCropDirExistenceAsserted: Boolean = false

    /**
     * Creates AutoCrop dir in external storage pictures directory if saveToAutocropDir
     * true after toggling, dir not yet existent and Version < Q, beginning from which
     * directories are created automatically
     */
    private fun saveToAutocropDirTogglingEcho() {
        if (saveToAutocropDir && !autoCropDirExistenceAsserted && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
            with(
                File(
                    Environment.getExternalStoragePublicDirectory(relativeCropSaveDirPath)
                        .toString()
                )
            ) {
                if (!exists())
                    mkdir().also {
                        Timber.i("Created $absolutePath")
                    }

                autoCropDirExistenceAsserted = true
            }
    }
}
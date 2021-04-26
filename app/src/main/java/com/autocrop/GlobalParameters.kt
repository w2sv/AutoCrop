package com.autocrop

import android.app.Activity
import android.os.Build
import android.os.Environment
import com.autocrop.utils.android.getSharedPreferencesBool
import com.autocrop.utils.android.writeSharedPreferencesBool
import com.autocrop.utils.toInt
import timber.log.Timber
import java.io.File


enum class Parameter{
    DELETE_INPUT_SCREENSHOTS,
    SAVE_TO_AUTOCROP_DIR;

    companion object {
        fun nameFromOrdinal(ordinal: Int): String =
            values().first { it.ordinal == ordinal }.name
    }
}


/**
 * Singleton encapsulating entirety of parameters directly set by user
 * having a global impact
 */
object GlobalParameters {
    var initialized: Boolean = false
    private val values: Array<Boolean> = Array(Parameter.values().size) { false }

    fun clone(): Array<Boolean> = values.clone()
    operator fun get(parameter: Parameter): Boolean = values[parameter.ordinal]
    operator fun set(parameter: Parameter, value: Boolean){
        values[parameter.ordinal] = value.also {
            Timber.i("Set GlobalParameters.${parameter.name} to $it")
        }
    }

    fun toggle(parameter: Parameter){
        this[parameter] = !this[parameter]
        Timber.i("Toggled ${parameter.name} to ${this[parameter]}")

        if (parameter == Parameter.SAVE_TO_AUTOCROP_DIR && saveToAutocropDir)
            saveToAutocropDirDownstreamActions()
    }

    fun besetFromSharedPreferences(activity: Activity){
        val defaultValues: List<Boolean> = listOf(false, true)

        values.indices.forEach {
            values[it] = activity.getSharedPreferencesBool(
                Parameter.nameFromOrdinal(it),
                defaultValues[it]
            )
        }

        initialized = true.also {
            Timber.i("Initialized Parameters")
        }
    }

    fun writeToSharedPreferences(previousValues: Array<Boolean>, activity: Activity){
        (values zip previousValues).forEachIndexed { index, valuePair ->
            with (valuePair) {
                if (first != second)
                    activity.writeSharedPreferencesBool(
                        Parameter.nameFromOrdinal(index),
                        first
                    ).also {
                        Timber.i("Set SharedPreferences.${Parameter.nameFromOrdinal(index)} to $first")
                    }
            }
        }
    }

    // ---------------deleteInputScreenshots-------------

    val deleteInputScreenshots: Boolean
        get() = this[Parameter.DELETE_INPUT_SCREENSHOTS]

    // -------------saveToAutocropDir--------------------

    val saveToAutocropDir: Boolean
        get() = this[Parameter.SAVE_TO_AUTOCROP_DIR]
    private var autoCropDirExistenceAsserted: Boolean = false

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

    /**
     * Creates AutoCrop dir in external storage pictures directory if saveToAutocropDir
     * true after toggling, dir not yet existent and Version < Q, beginning from which
     * directories are created automatically
     */
    private fun saveToAutocropDirDownstreamActions() {
        if (!autoCropDirExistenceAsserted && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
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
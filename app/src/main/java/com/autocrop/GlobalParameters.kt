package com.autocrop

import android.os.Build
import android.os.Environment
import com.autocrop.utils.toInt
import timber.log.Timber
import java.io.File
import kotlin.properties.Delegates


/**
 * Singleton encapsulating entirety of parameters directly set by user
 * having a pseudo-global impact
 */
object GlobalParameters {

    // ---------------deleteInputScreenshots-------------

    var deleteInputScreenshots by Delegates.notNull<Boolean>()

    // -------------saveToAutocropDir--------------------

    var saveToAutocropDir by Delegates.notNull<Boolean>()
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
    fun toggleSaveToAutocropDir() {
        saveToAutocropDir = !saveToAutocropDir

        if (saveToAutocropDir) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !autoCropDirExistenceAsserted)
                with(
                    File(
                        Environment.getExternalStoragePublicDirectory(relativeCropSaveDirPath)
                            .toString()
                    )
                ) {
                    if (!this.exists())
                        this.mkdir().also {
                            Timber.i("Created ${this.absolutePath}")
                        }

                    autoCropDirExistenceAsserted = true
                }
        }
    }
}
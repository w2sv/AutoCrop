package com.autocrop

import android.net.Uri
import android.os.Build
import android.os.Environment
import com.autocrop.utils.toInt
import java.io.File
import kotlin.properties.Delegates


object GlobalParameters {
    val imageCash: MutableMap<Uri, CropWithRetentionPercentage> = mutableMapOf()

    var deleteInputScreenshots by Delegates.notNull<Boolean>()
    var saveToAutocropDir by Delegates.notNull<Boolean>()

    /**
     * Creates AutoCrop dir in external storage pictures directory if saveToAutocropDir
     * true after toggling, dir not yet existent and Version < Q, beginning from which
     * directories are created automatically
     */
    fun toggleSaveToDedicatedDir(){
        saveToAutocropDir = !saveToAutocropDir

        if (saveToAutocropDir){
            // TODO: test automatic dir creation starting from Q

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                with(
                    File(
                        Environment.getExternalStoragePublicDirectory(relativeCropSaveDirPath).toString()
                    )
                ){
                if (!this.exists())
                    this.mkdir()
                }
        }
    }

    val relativeCropSaveDirPath: String
        get() = "${Environment.DIRECTORY_PICTURES}${listOf("", "${File.separator}AutoCropped")[saveToAutocropDir.toInt()]}"
}
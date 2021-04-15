package com.autocrop

import android.net.Uri
import android.os.Build
import android.os.Environment
import com.autocrop.utils.toInt
import java.io.File
import kotlin.properties.Delegates


private val DEFAULT_CROP_DESTINATION_PATH: String = Environment.DIRECTORY_PICTURES


object GlobalParameters {
    var deleteInputScreenshots by Delegates.notNull<Boolean>()
    var saveToAutocropDir by Delegates.notNull<Boolean>()

    fun toggleSaveToDedicatedDir(){
        saveToAutocropDir = !saveToAutocropDir

        if (saveToAutocropDir){
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                with(
                    File(
                        Environment.getExternalStoragePublicDirectory(cropSaveDirPath).toString()
                    )
                ){
                if (!this.exists())
                    this.mkdir()
                }
        }
    }

    val cropSaveDirPath: String
        get() = "$DEFAULT_CROP_DESTINATION_PATH${listOf("", "${File.separator}AutoCropped")[saveToAutocropDir.toInt()]}"

    val imageCash: MutableMap<Uri, CropWithRetentionPercentage> = mutableMapOf()
}
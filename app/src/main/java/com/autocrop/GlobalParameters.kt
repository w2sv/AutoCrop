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
        get() = "${Environment.DIRECTORY_PICTURES}${listOf("", "${File.separator}AutoCropped")[saveToAutocropDir.toInt()]}"
}
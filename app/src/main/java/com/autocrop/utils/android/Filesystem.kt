package com.autocrop.utils.android

import android.os.Build
import android.os.Environment
import com.autocrop.UserPreferences
import timber.log.Timber
import java.io.File


val picturesDir: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

/**
 * Returns:
 *      flag indicating whether or not dir has been newly created
 */
fun makeDirIfRequired(path: String): Boolean {
    with(File(path)) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !exists())
            return mkdirs().also {
                if (it)
                    Timber.i("Created $path")
                else
                    Timber.e("Couldn't create $path")
            }
    }
    return false
}
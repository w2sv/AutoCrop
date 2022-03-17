package com.autocrop.utils.android

import timber.log.Timber
import java.io.File


/**
 * Returns:
 *      flag indicating whether or not dir has been newly created
 */
fun makeDirIfRequired(path: String): Boolean {
    with(File(path)) {
        if (apiNotNewerThanQ && !exists())
            return mkdirs().also {
                if (it)
                    Timber.i("Created $path")
                else
                    Timber.e("Couldn't create $path")
            }
    }
    return false
}
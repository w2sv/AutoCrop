package com.autocrop.utils.android

import java.io.File


/**
 * Returns:
 *      flag indicating whether or not dir has been newly created
 */
fun makeDirIfRequired(path: String): Boolean {
    with(File(path)) {
        if (apiNotNewerThanQ && !exists())
            return mkdirs()
    }
    return false
}
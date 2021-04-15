package com.autocrop

import android.graphics.Bitmap


const val PACKAGE_NAME: String = "com.autocrop"
const val PREFERENCES_INSTANCE_NAME: String = "autocrop_preferences"

enum class PreferencesKey{
    DELETE_SCREENSHOTS,
    SAVE_TO_AUTOCROP_FOLDER
}

typealias CropWithRetentionPercentage = Pair<Bitmap, Int>
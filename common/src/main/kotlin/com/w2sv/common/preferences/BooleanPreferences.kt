package com.w2sv.common.preferences

import android.content.SharedPreferences
import com.w2sv.androidutils.typedpreferences.BooleanPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BooleanPreferences @Inject constructor(appPreferences: SharedPreferences) : BooleanPreferences(
    "autoScroll" to true,
    "deleteScreenshots" to false,
    sharedPreferences = appPreferences
) {
    var autoScroll by this
    var deleteScreenshots by this
}


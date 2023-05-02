package com.w2sv.common.preferences

import android.content.SharedPreferences
import com.w2sv.androidutils.typedpreferences.BooleanPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlobalFlags @Inject constructor(appPreferences: SharedPreferences) : BooleanPreferences(
    "comparisonInstructionsShown" to false,
    sharedPreferences = appPreferences
) {
    var comparisonInstructionsShown by this
}
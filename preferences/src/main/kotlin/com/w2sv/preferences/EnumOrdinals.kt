package com.w2sv.preferences

import android.content.SharedPreferences
import com.w2sv.typedpreferences.IntPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EnumOrdinals @Inject constructor(appPreferences: SharedPreferences) : IntPreferences(
    "cropAdjustmentMode" to 0,
    sharedPreferences = appPreferences
) {
    var cropAdjustmentMode by this
}
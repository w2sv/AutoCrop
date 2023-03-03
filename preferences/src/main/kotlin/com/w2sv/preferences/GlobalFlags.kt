package com.w2sv.preferences

import android.content.SharedPreferences
import com.w2sv.typedpreferences.BooleanPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlobalFlags @Inject constructor(appPreferences: SharedPreferences) : BooleanPreferences(
    "onboardingDone" to false,
    "comparisonInstructionsShown" to false,
    sharedPreferences = appPreferences
) {
    var onboardingDone by this
    var comparisonInstructionsShown by this
}
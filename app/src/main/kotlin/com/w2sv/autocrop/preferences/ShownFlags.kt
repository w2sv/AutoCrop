package com.w2sv.autocrop.preferences

import android.content.SharedPreferences
import com.w2sv.kotlinutils.delegates.AutoSwitch
import com.w2sv.typedpreferences.descendants.BooleanPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShownFlags @Inject constructor(sharedPreferences: SharedPreferences) : BooleanPreferences(
    "welcomeDialogsShown" to false,
    "cropPagerInstructionsShown" to false,
    "comparisonInstructionsShown" to false,
    "aboutFragmentInstructionsShown" to false,
    sharedPreferences = sharedPreferences
) {
    var welcomeDialogsShown by this
    var cropPagerInstructionsShown by AutoSwitch.Mapped(this, false)
    var comparisonInstructionsShown by AutoSwitch.Mapped(this, false)
    var aboutFragmentInstructionsShown by AutoSwitch.Mapped(this, false)
}
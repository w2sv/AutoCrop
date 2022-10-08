package com.autocrop.preferences

import android.content.SharedPreferences
import com.autocrop.utils.kotlin.delegates.AutoSwitch

object BooleanPreferences : TypedPreferences<Boolean>(
    mutableMapOf(
        "autoScroll" to true,
        "deleteScreenshots" to false,

        "welcomeMessageShown" to false,
        "viewPagerInstructionsShown" to false,
        "comparisonInstructionsShown" to false,
        "aboutFragmentInstructionsShown" to false
    )
) {
    /**
     * Expose values as delegated variables for convenience
     */
    var autoScroll by map
    var deleteScreenshots by map

    var welcomeMessageShown by AutoSwitch.Mapped(map, false)
    var viewPagerInstructionsShown by AutoSwitch.Mapped(map, false)
    var comparisonInstructionsShown by AutoSwitch.Mapped(map, false)
    var aboutFragmentInstructionsShown by AutoSwitch.Mapped(map, false)

    override fun SharedPreferences.writeValue(key: String, value: Boolean){
        edit().putBoolean(key, value).apply()
    }
    override fun SharedPreferences.getValue(key: String, defaultValue: Boolean): Boolean =
        getBoolean(key, defaultValue)
}
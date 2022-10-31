package com.w2sv.autocrop.preferences

import android.content.Context
import android.content.SharedPreferences
import android.widget.Switch
import com.w2sv.kotlinutils.delegates.AutoSwitch

object BooleanPreferences : TypedPreferences<Boolean>(
    mutableMapOf(
        "autoScroll" to true,
        "deleteScreenshots" to false,

        "welcomeDialogShown" to false,
        "cropPagerInstructionsShown" to false,
        "comparisonInstructionsShown" to false,
        "aboutFragmentInstructionsShown" to false
    )
) {
    /**
     * Expose values as delegated variables for convenience
     */
    var autoScroll by map
    var deleteScreenshots by map

    var welcomeDialogShown by AutoSwitch.Mapped(map, false)
    var cropPagerInstructionsShown by AutoSwitch.Mapped(map, false)
    var comparisonInstructionsShown by AutoSwitch.Mapped(map, false)
    var aboutFragmentInstructionsShown by AutoSwitch.Mapped(map, false)

    override fun SharedPreferences.writeValue(key: String, value: Boolean) {
        edit().putBoolean(key, value).apply()
    }

    override fun SharedPreferences.getValue(key: String, defaultValue: Boolean): Boolean =
        getBoolean(key, defaultValue)

    fun createSwitch(context: Context, key: String): Switch =
        Switch(context).apply {
            isChecked = getValue(key)
            setOnCheckedChangeListener { _, isChecked ->
                this@BooleanPreferences[key] = isChecked
            }
        }
}
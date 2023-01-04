package com.w2sv.autocrop.preferences

import android.content.Context
import android.content.SharedPreferences
import android.widget.Switch
import com.w2sv.kotlinutils.delegates.AutoSwitch
import com.w2sv.typedpreferences.descendants.BooleanPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BooleanPreferences @Inject constructor(sharedPreferences: SharedPreferences) : BooleanPreferences(
    "autoScroll" to true,
    "deleteScreenshots" to false,

    "welcomeDialogsShown" to false,
    "cropPagerInstructionsShown" to false,
    "comparisonInstructionsShown" to false,
    "aboutFragmentInstructionsShown" to false,
    sharedPreferences = sharedPreferences
) {

    var autoScroll by this
    var deleteScreenshots by this

    var welcomeDialogsShown by this
    var cropPagerInstructionsShown by AutoSwitch.Mapped(this, false)
    var comparisonInstructionsShown by AutoSwitch.Mapped(this, false)
    var aboutFragmentInstructionsShown by AutoSwitch.Mapped(this, false)

    fun createSwitch(context: Context, key: String): Switch =
        Switch(context).apply {
            isChecked = getValue(key)
            setOnCheckedChangeListener { _, isChecked ->
                this@BooleanPreferences[key] = isChecked
            }
        }
}
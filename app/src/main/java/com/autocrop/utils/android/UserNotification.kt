package com.autocrop.utils.android

import android.app.Activity
import android.text.SpannableStringBuilder
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.w2sv.autocrop.R
import de.mateware.snacky.Snacky

object NotificationColor {
    const val SUCCESS: Int = R.color.light_green
}

fun Activity.displaySnackbar(text: String, @DrawableRes icon: Int? = null, @ColorRes textColorRes: Int? = null) =
    Snacky
        .builder()
        .setText(text)
        .styleAndShow(this, icon, textColorRes)

fun Activity.displaySnackbar(text: SpannableStringBuilder, @DrawableRes icon: Int? = null, @ColorRes textColorRes: Int? = null) =
    Snacky
        .builder()
        .setText(text)
        .styleAndShow(this, icon, textColorRes)

private fun Snacky.Builder.styleAndShow(activity: Activity, @DrawableRes icon: Int? = null, @ColorRes textColorRes: Int? = null) =
    centerText()
        .apply {
            icon?.let {
                setIcon(it)
            }
            textColorRes?.let {
                setTextColor(getColorInt(it, activity))
            }
    }
        .setDuration(Snacky.LENGTH_LONG)
        .setActivity(activity)
        .build()
        .show()

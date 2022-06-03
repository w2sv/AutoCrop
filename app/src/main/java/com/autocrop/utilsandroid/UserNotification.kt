package com.autocrop.utilsandroid

import android.app.Activity
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.w2sv.autocrop.R
import de.mateware.snacky.Snacky

object NotificationColor {
    const val SUCCESS: Int = R.color.light_green
}

fun Activity.snacky(
    text: CharSequence,
    @DrawableRes icon: Int? = null,
    @ColorRes textColorRes: Int? = null): Snacky.Builder =
        Snacky
            .builder()
            .setText(text)
            .centerText()
            .apply {
                icon?.let {
                    setIcon(it)
                }
                textColorRes?.let {
                    setTextColor(getColorInt(it, this@snacky))
                }
            }
            .setDuration(Snacky.LENGTH_LONG)
            .setActivity(this)

fun Snacky.Builder.buildAndShow() =
    build().show()
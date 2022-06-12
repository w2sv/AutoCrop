package com.autocrop.utilsandroid

import android.app.Activity
import androidx.annotation.ColorRes
import com.w2sv.autocrop.R
import de.mateware.snacky.Snacky

object NotificationColor {
    const val SUCCESS: Int = R.color.light_green
}

fun Activity.snacky(text: CharSequence, duration: Int = Snacky.LENGTH_LONG): Snacky.Builder =
        Snacky
            .builder()
            .setText(text)
            .centerText()
            .setDuration(duration)
            .setActivity(this)

fun Snacky.Builder.buildAndShow() =
    build().show()
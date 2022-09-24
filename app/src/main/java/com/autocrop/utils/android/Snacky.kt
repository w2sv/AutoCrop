package com.autocrop.utils.android

import android.app.Activity
import de.mateware.snacky.Snacky

fun Activity.snacky(text: CharSequence, duration: Int = Snacky.LENGTH_LONG): Snacky.Builder =
    Snacky
        .builder()
        .setText(text)
        .centerText()
        .setDuration(duration)
        .setActivity(this)

fun Snacky.Builder.buildAndShow() =
    build().show()
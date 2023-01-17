package com.w2sv.autocrop.utils.extensions

import android.app.Activity
import de.mateware.snacky.Snacky

fun Activity.snackyBuilder(text: CharSequence, duration: Int = Snacky.LENGTH_LONG): Snacky.Builder =
    Snacky.builder()
        .setText(text)
        .centerText()
        .setDuration(duration)
        .setActivity(this)

val Activity.playStoreLink: String
    get() = "https://play.google.com/store/apps/details?id=$packageName"
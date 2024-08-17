package com.w2sv.common.extensions

import android.app.Activity
import de.mateware.snacky.Snacky

fun Activity.getSnackyBuilder(text: CharSequence, duration: Int = Snacky.LENGTH_LONG): Snacky.Builder =
    Snacky.builder()
        .setText(text)
        .centerText()
        .setDuration(duration)
        .setActivity(this)
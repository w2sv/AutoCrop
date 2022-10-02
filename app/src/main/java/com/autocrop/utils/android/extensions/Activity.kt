package com.autocrop.utils.android.extensions

import android.app.Activity
import android.content.Intent
import android.net.Uri
import de.mateware.snacky.Snacky

fun Activity.goToWebpage(url: String){
    startActivity(
        Intent(
            "android.intent.action.VIEW",
            Uri.parse(url)
        )
    )
}

fun Activity.snacky(text: CharSequence, duration: Int = Snacky.LENGTH_LONG): Snacky.Builder =
    Snacky.builder()
        .setText(text)
        .centerText()
        .setDuration(duration)
        .setActivity(this)

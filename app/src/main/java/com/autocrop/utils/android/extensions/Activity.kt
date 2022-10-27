package com.autocrop.utils.android.extensions

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import de.mateware.snacky.Snacky

fun Activity.goToWebpage(url: String){
    startActivity(
        Intent(
            "android.intent.action.VIEW",
            Uri.parse(url)
        )
    )
}

fun Activity.snackyBuilder(text: CharSequence, duration: Int = Snacky.LENGTH_LONG, view: View? = null): Snacky.Builder =
    Snacky.builder()
        .setText(text)
        .centerText()
        .setDuration(duration)
        .setActivity(this)
        .setView(view)

fun Activity.hideSystemBars() {
    WindowCompat.getInsetsController(window, window.decorView).apply {
        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        hide(WindowInsetsCompat.Type.systemBars())
    }
}

fun Activity.showSystemBars() {
    WindowCompat.getInsetsController(window, window.decorView).apply {
        show(WindowInsetsCompat.Type.systemBars())
    }
}

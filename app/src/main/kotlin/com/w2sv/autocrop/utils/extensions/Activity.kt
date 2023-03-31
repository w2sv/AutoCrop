package com.w2sv.autocrop.utils.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.blogspot.atifsoftwares.animatoolib.Animatoo
import com.w2sv.autocrop.activities.main.MainActivity

fun Activity.startMainActivity(
    clear: Boolean = false,
    animation: ((Context) -> Unit)? = Animatoo::animateSwipeRight,
    configureIntent: (Intent.() -> Intent) = { this }
) {
    startActivity(
        Intent(
            this,
            MainActivity::class.java
        )
            .configureIntent()
            .apply { if (clear) flags = Intent.FLAG_ACTIVITY_CLEAR_TASK }
    )
    animation?.invoke(this)
}
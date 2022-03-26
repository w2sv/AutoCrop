package com.autocrop.utils.android

import android.app.Activity
import android.view.View
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.w2sv.autocrop.R


object TextColors {
    const val neutral: Int = R.color.light_gray
    const val successfullyCarriedOut: Int = R.color.light_green
    const val urgent: Int = R.color.saturated_magenta
}

fun Activity.displaySnackbar(
    message: String,
    textColor: Int,
    length: Int = Snackbar.LENGTH_LONG,
    backgroundColor: Int? = null) =

    Snackbar
        .make(findViewById(android.R.id.content), message, length)
        .apply {
            if (backgroundColor != null)
                setBackgroundTint(resources.getColor(backgroundColor, theme))

            with(view) {
                @Suppress("UNRESOLVED_REFERENCE")
                findViewById<TextView>(com.google.android.material.R.id.snackbar_text).apply {
                    setTextColor(resources.getColor(textColor, theme))
                    textAlignment = View.TEXT_ALIGNMENT_CENTER
                    maxLines = 2
                }
            }
        }
        .show()
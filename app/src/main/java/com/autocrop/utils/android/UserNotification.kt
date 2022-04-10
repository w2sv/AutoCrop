package com.autocrop.utils.android

import android.app.Activity
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.TextView
import androidx.annotation.ColorRes
import com.google.android.material.snackbar.Snackbar
import com.w2sv.autocrop.R

object NotificationColor {
    const val NEUTRAL: Int = R.color.light_gray
    const val SUCCESS: Int = R.color.light_green
    const val URGENT: Int = R.color.saturated_magenta
}

fun Activity.displaySnackbar(
    message: String,
    @ColorRes textColor: Int,
    length: Int = Snackbar.LENGTH_LONG) =
        Snackbar
            .make(findViewById(android.R.id.content), message, length)
            .apply {
                view.
                    configuredTextView()
                        .apply { setTextColor(getColorInt(textColor, context)) }
            }
            .show()

fun Activity.displaySnackbar(
    message: SpannableStringBuilder,
    length: Int = Snackbar.LENGTH_LONG) =
        Snackbar
            .make(findViewById(android.R.id.content), message, length)
            .apply {
                view.configuredTextView()
            }
            .show()

private fun View.configuredTextView(): TextView = @Suppress("UNRESOLVED_REFERENCE")
    findViewById<TextView>(com.google.android.material.R.id.snackbar_text).apply {
        textAlignment = View.TEXT_ALIGNMENT_CENTER
        maxLines = 2
    }
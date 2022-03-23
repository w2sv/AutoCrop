package com.autocrop.utils.android

import android.app.Activity
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.w2sv.autocrop.R
import com.google.android.material.snackbar.Snackbar


object TextColors {
    const val neutral: Int = R.color.light_gray
    const val successfullyCarriedOut: Int = R.color.light_green
    const val urgent: Int = R.color.saturated_magenta
}


fun Activity.displayToast(
    message: String,
    textColor: Int = TextColors.neutral,
    length: Int = Toast.LENGTH_LONG
) {

    Toast.makeText(
        this,
        message,
        length
    ).apply {
        with(view!!) {
            setBackgroundColor(Color.parseColor("darkgray"))

            findViewById<TextView>(android.R.id.message).apply {
                setTextColor(resources.getColor(textColor, theme))
                gravity = Gravity.CENTER
                with(Pair(16, 0)) {
                    setPadding(first, second, first, second)
                }
            }
        }
        show()
    }
}


fun Activity.displaySnackbar(
    message: String,
    textColor: Int,
    length: Int = Snackbar.LENGTH_LONG,
    backgroundColor: Int? = null
) = Snackbar.make(findViewById(android.R.id.content), message, length)
    .apply {
        backgroundColor?.let {
            setBackgroundTint(resources.getColor(it, theme))
        }

        with(view) {
            findViewById<TextView>(com.google.android.material.R.id.snackbar_text).apply {
                setTextColor(resources.getColor(textColor, theme))
                textAlignment = View.TEXT_ALIGNMENT_CENTER
                maxLines = 2
            }
        }
    }
    .show()
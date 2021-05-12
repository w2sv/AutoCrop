package com.autocrop.utils.android

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.bunsenbrenner.screenshotboundremoval.R
import com.google.android.material.snackbar.Snackbar


fun Activity.displayToast(message: String) {
    Toast.makeText(
        this,
        message,
        Toast.LENGTH_LONG
    ).apply {
        with(view!!) {
            setBackgroundColor(Color.parseColor("darkgray"))

            findViewById<TextView>(android.R.id.message).apply {
                setTextColor(Color.parseColor("white"))
                gravity = Gravity.CENTER
                with(Pair(14, 0)) {
                    setPadding(first, second, first, second)
                }
            }
        }
        show()
    }
}

fun Activity.displaySnackbar(message: String, textColorId: Int = R.color.light_green){
    Snackbar.make(
        findViewById(android.R.id.content),
        message,
        Snackbar.LENGTH_LONG
    )
        .apply{
            with(view){
                findViewById<TextView>(com.google.android.material.R.id.snackbar_text).apply {
                    setTextColor(resources.getColor(textColorId, theme))
                    textAlignment = View.TEXT_ALIGNMENT_CENTER
                    maxLines = 2
                }
            }
        }
        .show()
}

class SnackbarArgumentRetriever{
    private var retrieved: Boolean = false

    operator fun invoke(intent: Intent, extraName: String, defaultValue: Int): Int? =
        intent.getIntExtra(extraName, defaultValue).run {
            if (!retrieved && !equals(defaultValue))
                this
                    .also { retrieved = true }
            else
                null
        }
}
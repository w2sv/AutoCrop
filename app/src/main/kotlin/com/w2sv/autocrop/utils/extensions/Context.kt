package com.w2sv.autocrop.utils.extensions

import android.content.Context
import android.widget.Toast

fun Context.showToast(text: CharSequence, duration: Int = Toast.LENGTH_SHORT) {
    Toast
        .makeText(this, text, duration)
        .show()
}
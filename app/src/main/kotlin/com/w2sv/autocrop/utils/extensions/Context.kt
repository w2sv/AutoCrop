package com.w2sv.autocrop.utils.extensions

import android.content.ActivityNotFoundException
import android.content.Context
import android.widget.Toast
import com.w2sv.androidutils.generic.openUrl
import com.w2sv.androidutils.notifying.showToast

fun Context.openUrlWithActivityNotFoundHandling(url: String) {
    try {
        openUrl(url)
    }
    catch (e: ActivityNotFoundException) {
        showToast("Couldn't find a browser to open the URL with", Toast.LENGTH_LONG)
    }
}
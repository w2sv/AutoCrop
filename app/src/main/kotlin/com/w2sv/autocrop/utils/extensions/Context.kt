package com.w2sv.autocrop.utils.extensions

import android.content.ActivityNotFoundException
import android.content.Context
import android.widget.Toast
import com.w2sv.androidutils.generic.openUrl
import com.w2sv.androidutils.notifying.showToast
import com.w2sv.autocrop.R

fun Context.openUrlWithActivityNotFoundHandling(url: String) {
    try {
        openUrl(url)
    }
    catch (e: ActivityNotFoundException) {
        showToast(getString(R.string.couldn_t_find_a_browser_to_open_the_url_with), Toast.LENGTH_LONG)
    }
}
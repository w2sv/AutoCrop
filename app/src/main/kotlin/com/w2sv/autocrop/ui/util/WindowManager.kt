package com.w2sv.autocrop.ui.util

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

val WindowManager.resolution: Point
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Point(
            currentWindowMetrics.bounds.width(),
            currentWindowMetrics.bounds.height()
        )
    } else {
        Point().apply {
            @Suppress("DEPRECATION")
            defaultDisplay.getRealSize(this)
        }
    }

@Composable
fun rememberScreenResolution(): Point {
    val context = LocalContext.current
    return remember { getScreenResolution(context) }
}

@Suppress("DEPRECATION")
private fun getScreenResolution(context: Context): Point {
    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val bounds = wm.currentWindowMetrics.bounds
        Point(bounds.width(), bounds.height())
    } else {
        val display = wm.defaultDisplay
        Point().apply { display.getRealSize(this) }
    }
}

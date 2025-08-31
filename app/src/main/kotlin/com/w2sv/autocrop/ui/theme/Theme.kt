package com.w2sv.autocrop.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        typography = typography,
        colorScheme = colorScheme()
    ) {
        content()
    }
}

@Composable
private fun colorScheme(): ColorScheme {
    val magentaBright = colorResource(com.w2sv.core.common.R.color.magenta_bright)
    val magentaSaturated = colorResource(com.w2sv.core.common.R.color.magenta_saturated)
    val magentaDark = colorResource(com.w2sv.core.common.R.color.magenta_dark)
    val purple = colorResource(com.w2sv.core.common.R.color.purple)

    return darkColorScheme(onBackground = Color.White, onSurface = Color.White, primary = magentaBright)
}

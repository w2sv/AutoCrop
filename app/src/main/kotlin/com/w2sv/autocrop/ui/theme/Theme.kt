package com.w2sv.autocrop.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.toFontFamily
import com.w2sv.autocrop.R

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        typography = typography(),
        colorScheme = colorScheme(),
        content = content
    )
}

private val defaultTypography = Typography()
private val jost = Font(R.font.montserrat).toFontFamily()

private fun typography(): Typography =
    Typography(
        displayLarge = defaultTypography.displayLarge.copy(fontFamily = jost),
        displayMedium = defaultTypography.displayMedium.copy(fontFamily = jost),
        displaySmall = defaultTypography.displaySmall.copy(fontFamily = jost),

        headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = jost),
        headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = jost),
        headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = jost),

        titleLarge = defaultTypography.titleLarge.copy(fontFamily = jost),
        titleMedium = defaultTypography.titleMedium.copy(fontFamily = jost),
        titleSmall = defaultTypography.titleSmall.copy(fontFamily = jost),

        bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = jost),
        bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = jost),
        bodySmall = defaultTypography.bodySmall.copy(fontFamily = jost),

        labelLarge = defaultTypography.labelLarge.copy(fontFamily = jost),
        labelMedium = defaultTypography.labelMedium.copy(fontFamily = jost),
        labelSmall = defaultTypography.labelSmall.copy(fontFamily = jost)
    )

@Composable
private fun colorScheme(): ColorScheme {
    val magentaBright = colorResource(com.w2sv.core.common.R.color.magenta_bright)
    val magentaSaturated = colorResource(com.w2sv.core.common.R.color.magenta_saturated)
    val magentaDark = colorResource(com.w2sv.core.common.R.color.magenta_dark)
    val purple = colorResource(com.w2sv.core.common.R.color.purple)

    return darkColorScheme(onBackground = Color.White, onSurface = Color.White, primary = magentaBright)
}

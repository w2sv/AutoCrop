package com.w2sv.autocrop.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

val ColorScheme.onSurfaceVariantLowAlpha: Color
    @Composable
    @ReadOnlyComposable
    get() = onSurfaceVariant.copy(0.6f)

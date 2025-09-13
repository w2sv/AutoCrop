package com.w2sv.autocrop.ui.util.compose

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavController

val LocalNavController = staticCompositionLocalOf<NavController> { noCompositionLocalProvidedFor("LocalNavController") }

private fun noCompositionLocalProvidedFor(name: String): Nothing {
    error("$name not provided")
}

package com.w2sv.autocrop.ui.screen.home

import androidx.compose.runtime.Composable
import com.w2sv.autocrop.ui.ComposeAppFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeScreenFragment : ComposeAppFragment() {

    @Composable
    override fun ScreenContent() {
        HomeScreen()
    }
}

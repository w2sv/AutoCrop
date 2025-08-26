package com.w2sv.autocrop.ui.screen.home

import androidx.compose.runtime.Composable
import com.w2sv.autocrop.ui.screen.home.compose.HomeScreen
import com.w2sv.autocrop.util.ComposeFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeScreenFragment : ComposeFragment() {

    @Composable
    override fun ScreenContent() {
        HomeScreen(navController = navController)
    }
}
